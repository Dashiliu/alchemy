/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.dfire.platform.alchemy.connectors.rocketmq;

import static com.dfire.platform.alchemy.connectors.rocketmq.RocketMQConsumerProperties.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.Validate;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.consumer.*;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.dfire.platform.alchemy.connectors.rocketmq.serialization.KeyValueDeserializationSchema;

/**
 * The RocketMQSource is based on RocketMQ pull consumer mode, and provides exactly once reliability guarantees when
 * checkpoints are enabled. Otherwise, the source doesn't provide any reliability guarantees.
 */
public class RocketMQConsumer<OUT> extends RichParallelSourceFunction<OUT>
    implements CheckpointedFunction, ResultTypeQueryable<OUT> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(RocketMQConsumer.class);
    private static final String OFFSETS_STATE_NAME = "topic-partition-offset-states";
    private transient MQPullConsumerScheduleService pullConsumerScheduleService;
    private DefaultMQPullConsumer consumer;
    private KeyValueDeserializationSchema<OUT> schema;
    private AtomicBoolean running;
    private transient ListState<Tuple2<MessageQueue, Long>> unionOffsetStates;
    private Map<MessageQueue, Long> offsetTable;
    private Map<MessageQueue, Long> restoredOffsets;
    private RocketMQConsumerProperties props;
    private transient volatile boolean restored;

    public RocketMQConsumer(KeyValueDeserializationSchema<OUT> schema, RocketMQConsumerProperties props) {
        this.schema = schema;
        this.props = props;
    }

    public static void buildConsumerConfigs(RocketMQConsumerProperties props, DefaultMQPullConsumer consumer) {
        consumer.setNamesrvAddr(props.getNameServers());
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setPersistConsumerOffsetInterval(props.getPersistConsumerOffsetInterval());
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        LOG.debug("source open....");
        Validate.notNull(props, "Rocketmq Consumer properties can not be empty");
        Validate.notNull(schema, "KeyValueDeserializationSchema can not be null");
        Validate.notEmpty(props.getTopic(), "Consumer topic can not be empty");
        Validate.notEmpty(props.getConsumerGroup(), "Consumer group can not be empty");

        if (offsetTable == null) {
            offsetTable = new ConcurrentHashMap<>();
        }
        if (restoredOffsets == null) {
            restoredOffsets = new ConcurrentHashMap<>();
        }

        running = new AtomicBoolean(true);

        pullConsumerScheduleService = new MQPullConsumerScheduleService(props.getConsumerGroup());
        consumer = pullConsumerScheduleService.getDefaultMQPullConsumer();

        consumer.setInstanceName(String.valueOf(getRuntimeContext().getIndexOfThisSubtask()));
        buildConsumerConfigs(props, consumer);
    }

    @Override
    public void run(SourceContext context) throws Exception {
        LOG.debug("source run....");
        // The lock that guarantees that record emission and state updates are atomic,
        // from the view of taking a checkpoint.
        final Object lock = context.getCheckpointLock();

        pullConsumerScheduleService.setPullThreadNums(props.getPullPoolSize());
        pullConsumerScheduleService.registerPullTaskCallback(props.getTopic(), new PullTaskCallback() {

            @Override
            public void doPullTask(MessageQueue mq, PullTaskContext pullTaskContext) {
                try {
                    long offset = getMessageQueueOffset(mq);
                    if (offset < 0) {
                        return;
                    }

                    PullResult pullResult = consumer.pull(mq, props.getTag(), offset, props.getPullBatchSize());
                    boolean found = false;
                    switch (pullResult.getPullStatus()) {
                        case FOUND:
                            List<MessageExt> messages = pullResult.getMsgFoundList();
                            for (MessageExt msg : messages) {
                                byte[] key
                                    = msg.getKeys() != null ? msg.getKeys().getBytes(StandardCharsets.UTF_8) : null;
                                byte[] value = msg.getBody();
                                OUT data = schema.deserializeKeyAndValue(key, value);

                                // output and state update are atomic
                                synchronized (lock) {
                                    context.collectWithTimestamp(data, msg.getBornTimestamp());
                                }
                            }
                            found = true;
                            break;
                        case NO_MATCHED_MSG:
                            LOG.debug("No matched message after offset {} for queue {}", offset, mq);
                            break;
                        case NO_NEW_MSG:
                            break;
                        case OFFSET_ILLEGAL:
                            LOG.warn("Offset {} is illegal for queue {}", offset, mq);
                            break;
                        default:
                            break;
                    }

                    synchronized (lock) {
                        putMessageQueueOffset(mq, pullResult.getNextBeginOffset());
                    }

                    if (found) {
                        pullTaskContext.setPullNextDelayTimeMillis(0); // no delay when messages were found
                    } else {
                        pullTaskContext.setPullNextDelayTimeMillis(props.getDelayWhenMessageNotFound());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
            pullConsumerScheduleService.start();
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }

        running.set(true);

        awaitTermination();

    }

    private void awaitTermination() throws InterruptedException {
        while (running.get()) {
            Thread.sleep(50);
        }
    }

    private long getMessageQueueOffset(MessageQueue mq) throws MQClientException {
        Long offset = offsetTable.get(mq);
        // restoredOffsets(unionOffsetStates) is the restored global union state;
        // should only snapshot mqs that actually belong to us
        if (restored && offset == null) {
            offset = restoredOffsets.get(mq);
        }
        if (offset == null) {
            offset = consumer.fetchConsumeOffset(mq, false);
            if (offset < 0) {
                String consumeFromWhere = props.getConsumeFromWhere();
                switch (consumeFromWhere) {
                    case CONSUMER_FROM_EARLIEST:
                        offset = consumer.minOffset(mq);
                        break;
                    case CONSUMER_FROM_LATEST:
                        offset = consumer.maxOffset(mq);
                        break;
                    case CONSUMER_FROM_TIMESTAMP:
                        offset = consumer.searchOffset(mq, props.getConsumeTimestamp());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown value for CONSUMER_OFFSET_RESET_TO.");
                }
            }
            offsetTable.put(mq, offset);
        }

        return offsetTable.get(mq);
    }

    private void putMessageQueueOffset(MessageQueue mq, long offset) throws MQClientException {
        offsetTable.put(mq, offset);
        consumer.updateConsumeOffset(mq, offset);
    }

    @Override
    public void cancel() {
        LOG.debug("cancel ...");
        running.set(false);

        if (pullConsumerScheduleService != null) {
            pullConsumerScheduleService.shutdown();
        }

        offsetTable.clear();
        restoredOffsets.clear();
    }

    @Override
    public void close() throws Exception {
        LOG.debug("close ...");
        // pretty much the same logic as cancelling
        try {
            cancel();
        } finally {
            super.close();
        }
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        // called when a snapshot for a checkpoint is requested

        if (!running.get()) {
            LOG.debug("snapshotState() called on closed source; returning null.");
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Snapshotting state {} ...", context.getCheckpointId());
        }

        unionOffsetStates.clear();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Snapshotted state, last processed offsets: {}, checkpoint id: {}, timestamp: {}", offsetTable,
                context.getCheckpointId(), context.getCheckpointTimestamp());
        }

        for (Map.Entry<MessageQueue, Long> entry : offsetTable.entrySet()) {
            unionOffsetStates.add(Tuple2.of(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        // called every time the user-defined function is initialized,
        // be that when the function is first initialized or be that
        // when the function is actually recovering from an earlier checkpoint.
        // Given this, initializeState() is not only the place where different types of state are initialized,
        // but also where state recovery logic is included.
        LOG.debug("initialize State ...");

        this.unionOffsetStates
            = context.getOperatorStateStore().getUnionListState(new ListStateDescriptor<>(OFFSETS_STATE_NAME,
                TypeInformation.of(new TypeHint<Tuple2<MessageQueue, Long>>() {})));

        this.restored = context.isRestored();

        if (restored) {
            if (restoredOffsets == null) {
                restoredOffsets = new ConcurrentHashMap<>();
            }
            for (Tuple2<MessageQueue, Long> mqOffsets : unionOffsetStates.get()) {
                // unionOffsetStates is the restored global union state;
                // should only snapshot mqs that actually belong to us
                restoredOffsets.put(mqOffsets.f0, mqOffsets.f1);
            }
            LOG.info("Setting restore state in the consumer. Using the following offsets: {}", restoredOffsets);
        } else {
            LOG.info("No restore state for the consumer.");
        }
    }

    @Override
    public TypeInformation<OUT> getProducedType() {
        return schema.getProducedType();
    }
}
