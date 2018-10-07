package com.dfire.platform.alchemy.connectors.rocketmq;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.Validate;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.table.shaded.org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.dfire.platform.alchemy.api.selector.TopicSelector;
import com.dfire.platform.alchemy.connectors.rocketmq.serialization.KeyValueSerializationSchema;

public class RocketMQProducer<IN> extends RichSinkFunction<IN> implements CheckpointedFunction {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(RocketMQProducer.class);
    private final KeyValueSerializationSchema<IN> serializationSchema;
    private final TopicSelector topicSelector;
    private transient DefaultMQProducer producer;
    private RocketMQProducerProperties props;

    public RocketMQProducer(KeyValueSerializationSchema<IN> schema, RocketMQProducerProperties props) {
        this(schema, props, null);
    }

    public RocketMQProducer(KeyValueSerializationSchema<IN> schema, RocketMQProducerProperties props,
        TopicSelector topicSelector) {
        this.serializationSchema = schema;
        this.props = props;
        this.topicSelector = topicSelector;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        Validate.notNull(props, "Producer properties can not be empty");
        Validate.notNull(serializationSchema, "KeyValueSerializationSchema can not be null");

        producer = new DefaultMQProducer();
        producer.setInstanceName(String.valueOf(getRuntimeContext().getIndexOfThisSubtask()));
        buildProducerConfigs(props, producer);
        try {
            producer.start();
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void invoke(IN input, Context context) throws Exception {
        Message msg = prepareMessage(input);
        if (props.isAsync()) {
            try {
                producer.send(msg, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        LOG.debug("Async send message success! result: {}", sendResult);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        if (throwable != null) {
                            LOG.error("Async send message failure!", throwable);
                        }
                    }
                });
            } catch (Exception e) {
                LOG.error("Async send message failure!", e);
            }
        } else {
            // sync sending, will return a SendResult
            try {
                SendResult result = producer.send(msg);
                LOG.debug("Sync send message result: {}", result);
            } catch (Exception e) {
                LOG.error("Sync send message failure!", e);
            }
        }
    }

    private Message prepareMessage(IN input) {
        byte[] k = serializationSchema.serializeKey(input);
        String key = k != null ? new String(k, StandardCharsets.UTF_8) : "";
        byte[] value = serializationSchema.serializeValue(input);

        Validate.notNull(value, "the message body is null");
        String topic = props.getTopic();
        String tag = props.getTag();
        if (StringUtils.isEmpty(topic) && topicSelector != null) {
            topic = topicSelector.getTopic(input);
            tag = topicSelector.getTag(input);
        }
        Validate.notNull(value, "the topic is null");
        Validate.notNull(value, "the tag is null");

        Message msg = new Message(topic, tag, key, value);
        return msg;
    }

    private void buildProducerConfigs(RocketMQProducerProperties props, DefaultMQProducer producer) {
        producer.setNamesrvAddr(props.getNameServers());
        producer.setProducerGroup(props.getProducerGroup());
        producer.setRetryTimesWhenSendFailed(props.getRetryTimesWhenSendFailed());
        producer.setSendMsgTimeout(props.getSendMsgTimeout());
    }

    @Override
    public void close() throws Exception {
        if (producer != null) {
            producer.shutdown();
        }
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {

    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        // nothing to do
    }
}
