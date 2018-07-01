package com.dfire.platform.alchemy.web.descriptor;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.Kafka010JsonTableSource;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.sources.tsextractors.ExistingField;
import org.apache.flink.table.sources.tsextractors.StreamRecordTimestamp;
import org.apache.flink.table.sources.tsextractors.TimestampExtractor;
import org.apache.flink.table.sources.wmstrategies.BoundedOutOfOrderTimestamps;
import org.apache.flink.table.sources.wmstrategies.WatermarkStrategy;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.web.bind.BindPropertiesFactory;
import com.dfire.platform.alchemy.web.common.*;
import com.dfire.platform.alchemy.web.util.PropertiesUtils;
import com.dfire.platform.alchemy.web.util.TypeUtils;

/**
 * @author congbai
 * @date 02/06/2018
 */
public class SourceDescriptor implements CoreDescriptor {

    private String name;

    private List<Field> input;

    private List<Field> schema;

    private Map<String, Object> connector;

    private volatile ConnectorDescriptor connectorDescriptor;

    private FormatDescriptor format;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        if (ClusterType.FLINK.equals(clusterType)) {
            return transformFlink();
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);
    }

    public List<Field> getInput() {
        return input;
    }

    public void setInput(List<Field> input) {
        this.input = input;
    }

    public List<Field> getSchema() {
        return schema;
    }

    public void setSchema(List<Field> schema) {
        this.schema = schema;
    }

    public Map<String, Object> getConnector() {
        return connector;
    }

    public void setConnector(Map<String, Object> connector) {
        this.connector = connector;
    }

    public ConnectorDescriptor getConnectorDescriptor() {
        if (this.connectorDescriptor == null) {
            synchronized (this) {
                if (this.connector == null) {
                    return this.connectorDescriptor;
                }
                Object type = this.connector.get(Constants.DESCRIPTOR_TYPE_KEY);
                if (type == null) {
                    return this.connectorDescriptor;
                }
                ConnectorDescriptor connectorDescriptor
                    = DescriptorFactory.me.find(String.valueOf(type), ConnectorDescriptor.class);
                if (connectorDescriptor == null) {
                    return this.connectorDescriptor;
                }
                try {
                    this.connectorDescriptor = connectorDescriptor.getClass().newInstance();
                    BindPropertiesFactory.bindProperties(this.connectorDescriptor, "",
                        PropertiesUtils.createProperties(this.connector));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return connectorDescriptor;
    }

    public void setConnectorDescriptor(ConnectorDescriptor connectorDescriptor) {
        this.connectorDescriptor = connectorDescriptor;
    }

    public FormatDescriptor getFormat() {
        return format;
    }

    public void setFormat(FormatDescriptor format) {
        this.format = format;
    }

    @Override
    public String getType() {
        return Constants.TYPE_VALUE_SOURCE;
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(name, "table的名称不能为空");
        Assert.notNull(schema, "table字段不能为空");
        Assert.isTrue(schema.size() > 0, "table字段不能为空");
        connectorDescriptor.validate();
    }

    private <T> T transformFlink() throws Exception {
        if (this.getConnectorDescriptor() instanceof KafkaConnectorDescriptor) {
            return buildKafkaSource();
        }
        return null;
    }

    private <T> T buildKafkaSource() throws ClassNotFoundException {
        KafkaConnectorDescriptor descriptor = (KafkaConnectorDescriptor)this.getConnectorDescriptor();
        Kafka010JsonTableSource.Builder builder = Kafka010JsonTableSource.builder();
        builder.forTopic(descriptor.getTopic());
        createSchema(this.schema, builder);
        if (StartupMode.EARLIEST.getMode().equals(descriptor.getStartupMode())) {
            builder.fromEarliest();
        }
        builder.withKafkaProperties(PropertiesUtils.fromYamlMap(descriptor.getProperties()));
        return (T)builder.build();
    }

    private void createSchema(List<Field> schema, Kafka010JsonTableSource.Builder builder)
        throws ClassNotFoundException {
        if (CollectionUtils.isEmpty(schema)) {
            return;
        }

        String[] columnNames = new String[schema.size()];
        TypeInformation[] columnTypes = new TypeInformation[schema.size()];
        for (int i = 0; i < schema.size(); i++) {
            columnNames[i] = schema.get(i).getName();
            TypeInformation typeInformation = TypeUtils.readTypeInfo(schema.get(i).getType());
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
                WatermarkStrategy watermarkStrategy
                    = Watermarks.Type.PERIODIC_BOUNDED.getType().equals(timeAttribute.getWatermarks().getType())
                        ? new BoundedOutOfOrderTimestamps(timeAttribute.getWatermarks().getDelay()) : null;
                builder.withRowtimeAttribute(schema.get(i).getName(), timestampExtractor, watermarkStrategy);
            }
        }
        TableSchema tableSchema = new TableSchema(columnNames, columnTypes);
        builder.withSchema(tableSchema);
    }

}
