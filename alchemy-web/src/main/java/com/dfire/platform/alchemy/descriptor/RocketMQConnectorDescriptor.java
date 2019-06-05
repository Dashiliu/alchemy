package com.dfire.platform.alchemy.descriptor;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.sources.RowtimeAttributeDescriptor;
import org.apache.flink.table.sources.tsextractors.ExistingField;
import org.apache.flink.table.sources.tsextractors.StreamRecordTimestamp;
import org.apache.flink.table.sources.tsextractors.TimestampExtractor;
import org.apache.flink.table.sources.wmstrategies.AscendingTimestamps;
import org.apache.flink.table.sources.wmstrategies.BoundedOutOfOrderTimestamps;
import org.apache.flink.table.sources.wmstrategies.PreserveWatermarks;
import org.apache.flink.table.sources.wmstrategies.WatermarkStrategy;
import org.apache.flink.table.typeutils.TypeStringUtils;
import org.apache.flink.types.Row;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.common.Constants;
import com.dfire.platform.alchemy.common.Field;
import com.dfire.platform.alchemy.common.TimeAttribute;
import com.dfire.platform.alchemy.common.Timestamps;
import com.dfire.platform.alchemy.common.Watermarks;
import com.dfire.platform.alchemy.connectors.rocketmq.RocketMQConsumerProperties;
import com.dfire.platform.alchemy.connectors.rocketmq.RocketMQTableSource;

/**
 * @author congbai
 * @date 2018/9/29
 */
public class RocketMQConnectorDescriptor implements ConnectorDescriptor {

    private String name;

    private String nameServers;

    private String topic;

    private String tag;

    private String consumerGroup;

    private String consumeFromWhere;

    private Long consumeTimestamp;

    private Integer pullPoolSize;

    private Integer pullBatchSiz;

    private Integer delayWhenMessageNotFound;

    private Integer persistConsumerOffsetInterval;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameServers() {
        return nameServers;
    }

    public void setNameServers(String nameServers) {
        this.nameServers = nameServers;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getConsumeFromWhere() {
        return consumeFromWhere;
    }

    public void setConsumeFromWhere(String consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }

    public Long getConsumeTimestamp() {
        return consumeTimestamp;
    }

    public void setConsumeTimestamp(Long consumeTimestamp) {
        this.consumeTimestamp = consumeTimestamp;
    }

    public Integer getPullPoolSize() {
        return pullPoolSize;
    }

    public void setPullPoolSize(Integer pullPoolSize) {
        this.pullPoolSize = pullPoolSize;
    }

    public Integer getPullBatchSiz() {
        return pullBatchSiz;
    }

    public void setPullBatchSiz(Integer pullBatchSiz) {
        this.pullBatchSiz = pullBatchSiz;
    }

    public Integer getDelayWhenMessageNotFound() {
        return delayWhenMessageNotFound;
    }

    public void setDelayWhenMessageNotFound(Integer delayWhenMessageNotFound) {
        this.delayWhenMessageNotFound = delayWhenMessageNotFound;
    }

    public Integer getPersistConsumerOffsetInterval() {
        return persistConsumerOffsetInterval;
    }

    public void setPersistConsumerOffsetInterval(Integer persistConsumerOffsetInterval) {
        this.persistConsumerOffsetInterval = persistConsumerOffsetInterval;
    }

    @Override
    public String type() {
        return Constants.CONNECTOR_TYPE_VALUE_ROCKETMQ;
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(topic, "topic不能为空");
        Assert.notNull(nameServers, "nameServers不能为空");
        Assert.notNull(consumerGroup, "consumerGroup不能为空");
    }

    @Override
    public <T> T buildSource(List<Field> schema, FormatDescriptor format) throws Exception {
        String[] columnNames = new String[schema.size()];
        TypeInformation[] columnTypes = new TypeInformation[schema.size()];
        String proctimeAttribute = null;
        RowtimeAttributeDescriptor rowtimeAttributeDescriptor = null;
        for (int i = 0; i < schema.size(); i++) {
            columnNames[i] = schema.get(i).getName();
            TypeInformation typeInformation = TypeStringUtils.readTypeInfo(schema.get(i).getType());
            if (typeInformation == null) {
                throw new UnsupportedOperationException("Unsupported type:" + schema.get(i).getType());
            }
            columnTypes[i] = typeInformation;
            if (schema.get(i).isProctime()) {
                proctimeAttribute = schema.get(i).getName();
            } else {
                TimeAttribute timeAttribute = schema.get(i).getRowtime();
                if (timeAttribute == null) {
                    continue;
                }
                TimestampExtractor timestampExtractor
                    = Timestamps.Type.FIELD.getType().equals(timeAttribute.getTimestamps().getType())
                        ? new ExistingField(timeAttribute.getTimestamps().getFrom()) : new StreamRecordTimestamp();
                if (timeAttribute.getWatermarks() != null) {
                    WatermarkStrategy watermarkStrategy = null;
                    if (Watermarks.Type.PERIODIC_ASCENDING.getType().equals(timeAttribute.getWatermarks().getType())) {
                        watermarkStrategy = new AscendingTimestamps();
                    } else if (Watermarks.Type.PERIODIC_BOUNDED.getType()
                        .equals(timeAttribute.getWatermarks().getType())) {
                        watermarkStrategy = new BoundedOutOfOrderTimestamps(timeAttribute.getWatermarks().getDelay());
                    } else if (Watermarks.Type.FROM_SOURCE.getType().equals(timeAttribute.getWatermarks().getType())) {
                        watermarkStrategy = PreserveWatermarks.INSTANCE();
                    }
                    rowtimeAttributeDescriptor = new RowtimeAttributeDescriptor(schema.get(i).getName(),
                        timestampExtractor, watermarkStrategy);
                }
            }
        }
        TypeInformation<Row> returnType = new RowTypeInfo(columnTypes, columnNames);
        DeserializationSchema<Row> deserializationSchema = format.transform(returnType);
        TableSchema tableSchema = new TableSchema(columnNames, columnTypes);
        RocketMQTableSource rocketMQTableSource
            = new RocketMQTableSource(deserializationSchema, returnType, tableSchema, createProperties());
        if (StringUtils.isNotEmpty(proctimeAttribute)) {
            rocketMQTableSource.setProctimeAttribute(proctimeAttribute);
        }
        if (rowtimeAttributeDescriptor != null) {
            rocketMQTableSource.setRowtimeAttributeDescriptors(Collections.singletonList(rowtimeAttributeDescriptor));
        }
        return (T)rocketMQTableSource;
    }

    private RocketMQConsumerProperties createProperties() {
        RocketMQConsumerProperties mqConsumerProperties = new RocketMQConsumerProperties();
        mqConsumerProperties.setTopic(this.getTopic());
        mqConsumerProperties.setNameServers(this.getNameServers());
        mqConsumerProperties.setConsumerGroup(this.getConsumerGroup());
        if (StringUtils.isNotEmpty(this.getTag())) {
            mqConsumerProperties.setTag(this.getTag());
        }
        if (this.consumeFromWhere != null) {
            mqConsumerProperties.setConsumeFromWhere(this.consumeFromWhere);
        }
        if (this.consumeTimestamp != null) {
            mqConsumerProperties.setConsumeTimestamp(this.consumeTimestamp);
        }
        if (this.pullPoolSize != null) {
            mqConsumerProperties.setPullPoolSize(this.pullPoolSize);
        }
        if (this.pullBatchSiz != null) {
            mqConsumerProperties.setPullBatchSize(this.pullBatchSiz);
        }
        if (this.delayWhenMessageNotFound != null) {
            mqConsumerProperties.setDelayWhenMessageNotFound(this.delayWhenMessageNotFound);
        }
        if (this.persistConsumerOffsetInterval != null) {
            mqConsumerProperties.setPersistConsumerOffsetInterval(this.persistConsumerOffsetInterval);
        }
        return mqConsumerProperties;
    }

}
