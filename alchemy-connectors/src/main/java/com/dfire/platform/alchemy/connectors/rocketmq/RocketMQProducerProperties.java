package com.dfire.platform.alchemy.connectors.rocketmq;

import java.io.Serializable;

/**
 * @author congbai
 * @date 2018/9/5
 */
public class RocketMQProducerProperties implements Serializable {

    public static final String DEFAULT_TAG = "*";
    public static final int DEFAULT_RETRY_TIMES = 2;
    public static final int SEND_MSG_TIMEOUT = 3000;
    public static final int ASYNC_TIMEOUT = 3000;
    private static final long serialVersionUID = 1L;
    private String nameServers;

    private String topic;

    private String tag = DEFAULT_TAG;

    private String producerGroup;

    private boolean async;

    private int retryTimesWhenSendFailed = DEFAULT_RETRY_TIMES;

    private int sendMsgTimeout = SEND_MSG_TIMEOUT;

    private int asyncTimeOut = ASYNC_TIMEOUT;

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

    public String getProducerGroup() {
        return producerGroup;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public int getRetryTimesWhenSendFailed() {
        return retryTimesWhenSendFailed;
    }

    public void setRetryTimesWhenSendFailed(int retryTimesWhenSendFailed) {
        this.retryTimesWhenSendFailed = retryTimesWhenSendFailed;
    }

    public int getSendMsgTimeout() {
        return sendMsgTimeout;
    }

    public void setSendMsgTimeout(int sendMsgTimeout) {
        this.sendMsgTimeout = sendMsgTimeout;
    }

    public int getAsyncTimeOut() {
        return asyncTimeOut;
    }

    public void setAsyncTimeOut(int asyncTimeOut) {
        this.asyncTimeOut = asyncTimeOut;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
}
