package com.dfire.platform.alchemy.web.descriptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.connectors.rocketmq.RocketMQProducerProperties;
import com.dfire.platform.alchemy.connectors.rocketmq.RocketMQTableSink;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;

/**
 * @author congbai
 * @date 06/06/2018
 */
public class RocketMQSinkDescriptor extends SinkDescriptor {

    private String name;

    private String nameServers;

    private String topic;

    private String tag;

    private String producerGroup;

    private Boolean async;

    private Integer retryTimesWhenSendFailed;

    private Integer sendMsgTimeout;

    private Integer asyncTimeOut;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getNameServers() {
        return nameServers;
    }

    public void setNameServers(String nameServers) {
        this.nameServers = nameServers;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getProducerGroup() {
        return producerGroup;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Integer getRetryTimesWhenSendFailed() {
        return retryTimesWhenSendFailed;
    }

    public void setRetryTimesWhenSendFailed(Integer retryTimesWhenSendFailed) {
        this.retryTimesWhenSendFailed = retryTimesWhenSendFailed;
    }

    public Integer getSendMsgTimeout() {
        return sendMsgTimeout;
    }

    public void setSendMsgTimeout(Integer sendMsgTimeout) {
        this.sendMsgTimeout = sendMsgTimeout;
    }

    public Integer getAsyncTimeOut() {
        return asyncTimeOut;
    }

    public void setAsyncTimeOut(Integer asyncTimeOut) {
        this.asyncTimeOut = asyncTimeOut;
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
        Assert.notNull(nameServers, "nameServers不能为空");
        Assert.notNull(producerGroup, "producerGroup不能为空");
    }

    private <T> T transformFlink() throws Exception {
        RocketMQProducerProperties producerProperties = createProperties();
        return (T)new RocketMQTableSink(producerProperties);
    }

    private RocketMQProducerProperties createProperties() {
        RocketMQProducerProperties rocketMQProducerProperties = new RocketMQProducerProperties();
        rocketMQProducerProperties.setTopic(this.getTopic());
        rocketMQProducerProperties.setNameServers(this.getNameServers());
        rocketMQProducerProperties.setProducerGroup(this.getProducerGroup());
        if (StringUtils.isNotEmpty(this.getTag())) {
            rocketMQProducerProperties.setTag(this.getTag());
        }
        if (this.async != null) {
            rocketMQProducerProperties.setAsync(this.async);
        }
        if (this.retryTimesWhenSendFailed != null) {
            rocketMQProducerProperties.setRetryTimesWhenSendFailed(this.retryTimesWhenSendFailed);
        }
        if (this.sendMsgTimeout != null) {
            rocketMQProducerProperties.setSendMsgTimeout(this.sendMsgTimeout);
        }
        if (this.asyncTimeOut != null) {
            rocketMQProducerProperties.setAsyncTimeOut(this.asyncTimeOut);
        }
        return rocketMQProducerProperties;
    }

    @Override
    public String type() {
        return Constants.SINK_TYPE_VALUE_ROCKETMQ;
    }
}
