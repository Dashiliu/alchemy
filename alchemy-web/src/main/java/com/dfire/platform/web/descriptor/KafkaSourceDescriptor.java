package com.dfire.platform.web.descriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.connectors.kafka.Kafka010JsonTableSource;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.sources.tsextractors.ExistingField;
import org.apache.flink.table.sources.wmstrategies.BoundedOutOfOrderTimestamps;
import org.apache.flink.util.Preconditions;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.dfire.platform.web.common.ClusterType;
import com.dfire.platform.web.common.Table;
import com.dfire.platform.web.common.TimeAttribute;
import com.dfire.platform.web.util.PropertiesUtils;

/**
 * todo 事件事件、处理时间 --> 水位
 * 
 * @author congbai
 * @date 03/06/2018
 */
@Component
public class KafkaSourceDescriptor extends SourceDescriptor {

    private String topic;

    private String brokers;

    private String consumerGroup;

    private String consumeFromWhere;

    private Map<String, String> properties;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBrokers() {
        return brokers;
    }

    public void setBrokers(String brokers) {
        this.brokers = brokers;
    }

    public String getConsumeFromWhere() {
        return consumeFromWhere;
    }

    public void setConsumeFromWhere(String consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String getContentType() {
        return "kafkaSource";
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        if (ClusterType.FLINK.equals(clusterType)) {
            return transformFlink();
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);
    }

    @Override
    public void validate() throws Exception {
        super.validate();
        Assert.notNull(topic, "topic不能为空");
        Assert.notNull(brokers, "brokers不能为空");
        Assert.notNull(consumerGroup, "consumerGroup不能为空");
    }

    private <T> T transformFlink() throws Exception {
        Map<String, String> prop = null;
        if (this.properties != null) {
            prop = new HashMap<>(this.properties);
        } else {
            prop = new HashMap<>();
        }

        prop.put(ConsumerConfig.GROUP_ID_CONFIG,
            Preconditions.checkNotNull(this.consumerGroup, "consumerGroup can not be null"));
        prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
            Preconditions.checkNotNull(this.brokers, "brokers can not be null"));
        if (StringUtils.isNotEmpty(this.consumeFromWhere)) {
            prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, this.consumeFromWhere);
        }
        Properties conf = PropertiesUtils.getProperties(prop);
        Kafka010JsonTableSource.Builder builder = Kafka010JsonTableSource.builder();
        builder.forTopic(this.topic).withKafkaProperties(conf);
        TableSchema inputSchema = createSchema(this.getInput());
        TableSchema tableSchema = createSchema(this.getOutput());
        if (inputSchema != null) {
            builder.forJsonSchema(inputSchema);
        }
        builder.withSchema(tableSchema);
        TimeAttribute timeAttribute = this.getTimeAttribute();
        if (timeAttribute != null && timeAttribute.getTimeCharacteristic() != null) {
            if (TimeCharacteristic.EventTime.toString().equalsIgnoreCase(timeAttribute.getTimeCharacteristic())) {
                builder.withRowtimeAttribute(timeAttribute.getAttribute(),
                    new ExistingField(timeAttribute.getAttribute()),
                    new BoundedOutOfOrderTimestamps(timeAttribute.getMaxOutOfOrderness()));
            } else {
                builder.withProctimeAttribute(timeAttribute.getAttribute());
            }
        }
        return (T)builder.build();
    }

    private TableSchema createSchema(Table table) throws ClassNotFoundException {
        if (table == null || CollectionUtils.isEmpty(table.getFields())) {
            return null;
        }
        String[] columnNames = new String[table.getFields().size()];
        TypeInformation[] columnTypes = new TypeInformation[table.getFields().size()];
        for (int i = 0; i < table.getFields().size(); i++) {
            columnNames[i] = table.getFields().get(i).getName();
            columnTypes[i] = TypeExtractor.createTypeInfo(Class.forName(table.getFields().get(i).getType()));
        }
        return new TableSchema(columnNames, columnTypes);
    }
}
