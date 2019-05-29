package com.dfire.platform.alchemy.web.descriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dfire.platform.alchemy.formats.grok.GrokRowDeserializationSchema;
import com.dfire.platform.alchemy.formats.hessian.HessianRowDeserializationSchema;
import com.dfire.platform.alchemy.formats.protostuff.ProtostuffRowDeserializationSchema;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.connectors.kafka.Kafka010JsonTableSource;
import org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartition;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.descriptors.KafkaValidator;
import org.apache.flink.table.sources.tsextractors.ExistingField;
import org.apache.flink.table.sources.tsextractors.StreamRecordTimestamp;
import org.apache.flink.table.sources.tsextractors.TimestampExtractor;
import org.apache.flink.table.sources.wmstrategies.AscendingTimestamps;
import org.apache.flink.table.sources.wmstrategies.BoundedOutOfOrderTimestamps;
import org.apache.flink.table.sources.wmstrategies.PreserveWatermarks;
import org.apache.flink.table.sources.wmstrategies.WatermarkStrategy;
import org.apache.flink.table.typeutils.TypeStringUtils;
import org.apache.flink.types.Row;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.connectors.kafka.AlchemyKafkaTableSource;
import com.dfire.platform.alchemy.web.common.*;
import com.dfire.platform.alchemy.web.util.PropertiesUtils;

/**
 * todo 事件事件、处理时间 --> 水位
 *
 * @author congbai
 * @date 03/06/2018
 */
public class KafkaConnectorDescriptor implements ConnectorDescriptor {

    private String topic;

    private String startupMode;

    private Map<String, String> specificOffsets;

    private Map<String, Object> properties;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getStartupMode() {
        return startupMode;
    }

    public void setStartupMode(String startupMode) {
        this.startupMode = startupMode;
    }

    public Map<String, String> getSpecificOffsets() {
        return specificOffsets;
    }

    public void setSpecificOffsets(Map<String, String> specificOffsets) {
        this.specificOffsets = specificOffsets;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(topic, "kafka的topic不能为空");
        Assert.notNull(properties, "kafka的properties不能为空");
        Assert.notNull(PropertiesUtils.fromYamlMap(this.properties).get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG),
            "kafak的" + ProducerConfig.BOOTSTRAP_SERVERS_CONFIG + "不能为空");

    }

    @Override
    public String type() {
        return Constants.CONNECTOR_TYPE_VALUE_KAFKA;
    }

    @Override
    public <T> T buildSource(ClusterType clusterType, List<Field> schema, FormatDescriptor format) throws Exception {
        if (ClusterType.FLINK.equals(clusterType)) {
            return buildKafkaFlinkSource(schema, format);
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);
    }

    private <T> T buildKafkaFlinkSource(List<Field> schema, FormatDescriptor format) throws ClassNotFoundException {
        String type = format.type();
        if (StringUtils.isEmpty(type)) {
            type = Constants.TYPE_VALUE_FORMAT_JSON;
        }
        switch (type) {
            case Constants.TYPE_VALUE_FORMAT_JSON:
                Kafka010JsonTableSource.Builder builder = new Kafka010JsonTableSource.Builder();
                createSchema(schema, builder);
                buildeProperties(builder);
                return (T)builder.build();
            case Constants.TYPE_VALUE_FORMAT_HESSIAN:
            case Constants.TYPE_VALUE_FORMAT_PB:
                AlchemyKafkaTableSource.Builder tableSourceBuilder = new AlchemyKafkaTableSource.Builder();
                TypeInformation<Row> returnType = createSchema(schema, tableSourceBuilder);
                buildeProperties(tableSourceBuilder);
                createDeserialization(returnType, format, tableSourceBuilder);
                return (T)tableSourceBuilder.build();
            case Constants.TYPE_VALUE_FORMAT_GROK:
                AlchemyKafkaTableSource.Builder grokTableSourceBuilder = new AlchemyKafkaTableSource.Builder();
                TypeInformation<Row> grokReturnType = createSchema(schema, grokTableSourceBuilder);
                buildeProperties(grokTableSourceBuilder);
                createDeserialization(grokReturnType, format, grokTableSourceBuilder);
                return (T)grokTableSourceBuilder.build();
            default:
        }
        return null;
    }

    private void createDeserialization(TypeInformation<Row> returnType, FormatDescriptor format,
        AlchemyKafkaTableSource.Builder tableSourceBuilder) throws ClassNotFoundException {
        DeserializationSchema<Row> deserializationSchema = null;
        switch (format.type()) {
            case Constants.TYPE_VALUE_FORMAT_HESSIAN:
                deserializationSchema
                    = new HessianRowDeserializationSchema(returnType, Class.forName(format.getClassName()));
                break;
            case Constants.TYPE_VALUE_FORMAT_PB:
                deserializationSchema
                    = new ProtostuffRowDeserializationSchema(returnType, Class.forName(format.getClassName()));
                break;
            case Constants.TYPE_VALUE_FORMAT_GROK:
                deserializationSchema
                    = new GrokRowDeserializationSchema(returnType, format.getRegular());
                break;
            default:
        }
        tableSourceBuilder.withDeserializationSchema(deserializationSchema);
    }

    private <T> void buildeProperties(T t) {
        if (t instanceof Kafka010JsonTableSource.Builder) {
            Kafka010JsonTableSource.Builder builder = (Kafka010JsonTableSource.Builder)t;
            builder.forTopic(this.topic);
            builder.withKafkaProperties(PropertiesUtils.fromYamlMap(this.properties));
            if (StringUtils.isNotEmpty(this.startupMode)) {
                switch (this.startupMode) {
                    case KafkaValidator.CONNECTOR_STARTUP_MODE_VALUE_EARLIEST:
                        builder.fromEarliest();
                        break;

                    case KafkaValidator.CONNECTOR_STARTUP_MODE_VALUE_LATEST:
                        builder.fromLatest();
                        break;

                    case KafkaValidator.CONNECTOR_STARTUP_MODE_VALUE_GROUP_OFFSETS:
                        builder.fromGroupOffsets();
                        break;

                    case KafkaValidator.CONNECTOR_STARTUP_MODE_VALUE_SPECIFIC_OFFSETS:
                        final Map<KafkaTopicPartition, Long> offsetMap = new HashMap<>();
                        for (Map.Entry<String, String> entry : this.specificOffsets.entrySet()) {
                            final KafkaTopicPartition topicPartition
                                = new KafkaTopicPartition(topic, Integer.parseInt(entry.getKey()));
                            offsetMap.put(topicPartition, Long.parseLong(entry.getValue()));
                        }
                        builder.fromSpecificOffsets(offsetMap);
                        break;
                    default:

                }
            }
        } else if (t instanceof AlchemyKafkaTableSource.Builder) {
            AlchemyKafkaTableSource.Builder builder = (AlchemyKafkaTableSource.Builder)t;
            builder.forTopic(this.topic);
            builder.withKafkaProperties(PropertiesUtils.fromYamlMap(this.properties));
            if (StringUtils.isNotEmpty(this.startupMode)) {
                switch (this.startupMode) {
                    case KafkaValidator.CONNECTOR_STARTUP_MODE_VALUE_EARLIEST:
                        builder.fromEarliest();
                        break;

                    case KafkaValidator.CONNECTOR_STARTUP_MODE_VALUE_LATEST:
                        builder.fromLatest();
                        break;

                    case KafkaValidator.CONNECTOR_STARTUP_MODE_VALUE_GROUP_OFFSETS:
                        builder.fromGroupOffsets();
                        break;

                    case KafkaValidator.CONNECTOR_STARTUP_MODE_VALUE_SPECIFIC_OFFSETS:
                        final Map<KafkaTopicPartition, Long> offsetMap = new HashMap<>();
                        for (Map.Entry<String, String> entry : this.specificOffsets.entrySet()) {
                            final KafkaTopicPartition topicPartition
                                = new KafkaTopicPartition(topic, Integer.parseInt(entry.getKey()));
                            offsetMap.put(topicPartition, Long.parseLong(entry.getValue()));
                        }
                        builder.fromSpecificOffsets(offsetMap);
                        break;
                    default:

                }
            }
        }
    }

    private <T> TypeInformation<Row> createSchema(List<Field> schema, T t) throws ClassNotFoundException {
        if (CollectionUtils.isEmpty(schema)) {
            return null;
        }
        if (t instanceof Kafka010JsonTableSource.Builder) {
            Kafka010JsonTableSource.Builder builder = (Kafka010JsonTableSource.Builder)t;
            String[] columnNames = new String[schema.size()];
            TypeInformation[] columnTypes = new TypeInformation[schema.size()];
            for (int i = 0; i < schema.size(); i++) {
                columnNames[i] = schema.get(i).getName();
                TypeInformation typeInformation = TypeStringUtils.readTypeInfo(schema.get(i).getType());
                if (typeInformation == null) {
                    throw new UnsupportedOperationException("Unsupported type:" + schema.get(i).getType());
                }
                columnTypes[i] = typeInformation;
                if (schema.get(i).isProctime()) {
                    builder.withProctimeAttribute(schema.get(i).getName());
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
                        if (Watermarks.Type.PERIODIC_ASCENDING.getType()
                            .equals(timeAttribute.getWatermarks().getType())) {
                            watermarkStrategy = new AscendingTimestamps();
                        } else if (Watermarks.Type.PERIODIC_BOUNDED.getType()
                            .equals(timeAttribute.getWatermarks().getType())) {
                            watermarkStrategy
                                = new BoundedOutOfOrderTimestamps(timeAttribute.getWatermarks().getDelay());
                        } else if (Watermarks.Type.FROM_SOURCE.getType()
                            .equals(timeAttribute.getWatermarks().getType())) {
                            watermarkStrategy = PreserveWatermarks.INSTANCE();
                        }
                        builder.withRowtimeAttribute(schema.get(i).getName(), timestampExtractor, watermarkStrategy);
                    }
                }
            }
            builder.withSchema(new TableSchema(columnNames, columnTypes));
        } else if (t instanceof AlchemyKafkaTableSource.Builder) {
            AlchemyKafkaTableSource.Builder builder = (AlchemyKafkaTableSource.Builder)t;
            String[] columnNames = new String[schema.size()];
            TypeInformation[] columnTypes = new TypeInformation[schema.size()];
            for (int i = 0; i < schema.size(); i++) {
                columnNames[i] = schema.get(i).getName();
                TypeInformation typeInformation = TypeStringUtils.readTypeInfo(schema.get(i).getType());
                if (typeInformation == null) {
                    throw new UnsupportedOperationException("Unsupported type:" + schema.get(i).getType());
                }
                columnTypes[i] = typeInformation;
                if (schema.get(i).isProctime()) {
                    builder.withProctimeAttribute(schema.get(i).getName());
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
                        if (Watermarks.Type.PERIODIC_ASCENDING.getType()
                            .equals(timeAttribute.getWatermarks().getType())) {
                            watermarkStrategy = new AscendingTimestamps();
                        } else if (Watermarks.Type.PERIODIC_BOUNDED.getType()
                            .equals(timeAttribute.getWatermarks().getType())) {
                            watermarkStrategy
                                = new BoundedOutOfOrderTimestamps(timeAttribute.getWatermarks().getDelay());
                        } else if (Watermarks.Type.FROM_SOURCE.getType()
                            .equals(timeAttribute.getWatermarks().getType())) {
                            watermarkStrategy = PreserveWatermarks.INSTANCE();
                        }
                        builder.withRowtimeAttribute(schema.get(i).getName(), timestampExtractor, watermarkStrategy);
                    }
                }
            }
            TypeInformation<Row> returnType = new RowTypeInfo(columnTypes, columnNames);
            builder.withReturnType(returnType);
            builder.withSchema(new TableSchema(columnNames, columnTypes));
            return returnType;
        }
        return null;
    }
}
