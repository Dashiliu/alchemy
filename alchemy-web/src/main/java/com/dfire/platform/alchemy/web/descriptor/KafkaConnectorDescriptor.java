package com.dfire.platform.alchemy.web.descriptor;

import java.util.Map;

import org.springframework.util.Assert;

import com.dfire.platform.alchemy.web.common.Constants;

/**
 * todo 事件事件、处理时间 --> 水位
 *
 * @author congbai
 * @date 03/06/2018
 */
public class KafkaConnectorDescriptor implements ConnectorDescriptor {

    private String topic;

    private String startupMode;

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

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(topic, "topic不能为空");
    }

    @Override
    public String getType() {
        return Constants.CONNECTOR_TYPE_VALUE_KAFKA;
    }
}
