package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.web.common.ReadMode;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;

/**
 * @author congbai
 * @date 03/06/2018
 */
public class RedisSinkDescriptor extends SinkDescriptor {

    private String name;

    private int readMode = ReadMode.CODE.getMode();

    private String sentinels;

    private String master;

    private int database;

    private Integer maxTotal;

    private Integer queueSize;

    private Integer threadNum;

    private String value;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSentinels() {
        return sentinels;
    }

    public void setSentinels(String sentinels) {
        this.sentinels = sentinels;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    public Integer getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
    }

    public int getReadMode() {
        return readMode;
    }

    public void setReadMode(int readMode) {
        this.readMode = readMode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        //// TODO: 2018/6/30
        return null;
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(sentinels, "redis的sentinels地址不能为空");
        Assert.notNull(master, "redis的master名称不能为空");
        Assert.notNull(database, "redis的database不能为空");
        if (maxTotal != null && maxTotal.intValue() > Constants.REDIS_MAX_TOTAL) {
            throw new IllegalArgumentException("redis最大连接数是：" + Constants.REDIS_MAX_TOTAL);
        }
        if (queueSize != null && queueSize.intValue() > Constants.REDIS_MAX_QUEUE_SIZE) {
            throw new IllegalArgumentException("redis队列最大数是：" + Constants.REDIS_MAX_QUEUE_SIZE);
        }
        if (threadNum != null && threadNum.intValue() > Constants.REDIS_MAX_THREAD_SIZE) {
            throw new IllegalArgumentException("redis最大消费线程是：" + Constants.REDIS_MAX_THREAD_SIZE);
        }
    }

    @Override
    public String getType() {
        return Constants.SINK_TYPE_VALUE_REDIS;
    }
}
