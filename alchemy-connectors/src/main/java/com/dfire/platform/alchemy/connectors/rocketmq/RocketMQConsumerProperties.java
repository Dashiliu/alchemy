package com.dfire.platform.alchemy.connectors.rocketmq;

import java.io.Serializable;

/**
 * @author congbai
 * @date 2018/9/5
 */
public class RocketMQConsumerProperties implements Serializable {

    public static final int DEFAULT_CONSUMER_OFFSET_PERSIST_INTERVAL = 5000;
    public static final int DEFAULT_CONSUMER_BATCH_SIZE = 32;
    public static final int DEFAULT_CONSUMER_PULL_POOL_SIZE = 20;
    public static final int DEFAULT_CONSUMER_DELAY_WHEN_MESSAGE_NOT_FOUND = 10;
    public static final String DEFAULT_CONSUMER_TAG = "*";
    public static final String CONSUMER_FROM_LATEST = "latest";
    public static final String CONSUMER_FROM_EARLIEST = "earliest";
    public static final String CONSUMER_FROM_TIMESTAMP = "timestamp";
    private static final long serialVersionUID = 1L;
    private String nameServers;

    private String topic;

    private String tag = DEFAULT_CONSUMER_TAG;

    // consumer

    private String consumerGroup;

    private String consumeFromWhere = CONSUMER_FROM_LATEST;

    private Long consumeTimestamp;

    private int pullPoolSize = DEFAULT_CONSUMER_PULL_POOL_SIZE;

    private int pullBatchSize = DEFAULT_CONSUMER_BATCH_SIZE;

    private int delayWhenMessageNotFound = DEFAULT_CONSUMER_DELAY_WHEN_MESSAGE_NOT_FOUND;

    private int persistConsumerOffsetInterval = DEFAULT_CONSUMER_OFFSET_PERSIST_INTERVAL;

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

    public int getPersistConsumerOffsetInterval() {
        return persistConsumerOffsetInterval;
    }

    public void setPersistConsumerOffsetInterval(int persistConsumerOffsetInterval) {
        this.persistConsumerOffsetInterval = persistConsumerOffsetInterval;
    }

    public int getPullPoolSize() {
        return pullPoolSize;
    }

    public void setPullPoolSize(int pullPoolSize) {
        this.pullPoolSize = pullPoolSize;
    }

    public int getPullBatchSize() {
        return pullBatchSize;
    }

    public void setPullBatchSize(int pullBatchSize) {
        this.pullBatchSize = pullBatchSize;
    }

    public int getDelayWhenMessageNotFound() {
        return delayWhenMessageNotFound;
    }

    public void setDelayWhenMessageNotFound(int delayWhenMessageNotFound) {
        this.delayWhenMessageNotFound = delayWhenMessageNotFound;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RocketMQConsumerProperties that = (RocketMQConsumerProperties)o;

        if (pullPoolSize != that.pullPoolSize)
            return false;
        if (pullBatchSize != that.pullBatchSize)
            return false;
        if (delayWhenMessageNotFound != that.delayWhenMessageNotFound)
            return false;
        if (persistConsumerOffsetInterval != that.persistConsumerOffsetInterval)
            return false;
        if (nameServers != null ? !nameServers.equals(that.nameServers) : that.nameServers != null)
            return false;
        if (topic != null ? !topic.equals(that.topic) : that.topic != null)
            return false;
        if (tag != null ? !tag.equals(that.tag) : that.tag != null)
            return false;
        if (consumerGroup != null ? !consumerGroup.equals(that.consumerGroup) : that.consumerGroup != null)
            return false;
        if (consumeFromWhere != null ? !consumeFromWhere.equals(that.consumeFromWhere) : that.consumeFromWhere != null)
            return false;
        return consumeTimestamp != null ? consumeTimestamp.equals(that.consumeTimestamp)
            : that.consumeTimestamp == null;

    }

    @Override
    public int hashCode() {
        int result = nameServers != null ? nameServers.hashCode() : 0;
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        result = 31 * result + (consumerGroup != null ? consumerGroup.hashCode() : 0);
        result = 31 * result + (consumeFromWhere != null ? consumeFromWhere.hashCode() : 0);
        result = 31 * result + (consumeTimestamp != null ? consumeTimestamp.hashCode() : 0);
        result = 31 * result + pullPoolSize;
        result = 31 * result + pullBatchSize;
        result = 31 * result + delayWhenMessageNotFound;
        result = 31 * result + persistConsumerOffsetInterval;
        return result;
    }
}
