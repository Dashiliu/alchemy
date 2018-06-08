package com.dfire.platform.web.descriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.flink.streaming.connectors.kafka.Kafka010JsonTableSink;
import org.apache.flink.util.Preconditions;
import org.apache.kafka.clients.CommonClientConfigs;
import org.springframework.util.Assert;

import com.dfire.platform.web.common.ClusterType;
import com.dfire.platform.web.util.PropertiesUtils;

/**
 * @author congbai
 * @date 06/06/2018
 */
public class KafkaSinkDescriptor extends SinkDescriptor {

    private String topic;

    private String brokers;

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

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
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
        Assert.notNull(topic, "topic不能为空");
        Assert.notNull(brokers, "brokers不能为空");
    }

    private <T> T transformFlink() throws Exception {
        Map<String, String> prop;
        if (this.properties != null) {
            prop = new HashMap<>(this.properties);
        } else {
            prop = new HashMap<>();
        }
        prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
            Preconditions.checkNotNull(this.brokers, "brokers can not be null"));
        Properties conf = PropertiesUtils.getProperties(prop);
        return (T)new Kafka010JsonTableSink(this.topic, conf);
    }
}
