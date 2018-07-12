package com.dfire.platform.alchemy.web.descriptor;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.api.sink.RedisInvoker;
import com.dfire.platform.alchemy.connectors.redis.Codis;
import com.dfire.platform.alchemy.connectors.redis.RedisProperties;
import com.dfire.platform.alchemy.connectors.redis.RedisTableSink;
import com.dfire.platform.alchemy.connectors.redis.Sentinel;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.common.ReadMode;

import redis.clients.jedis.JedisPoolConfig;

/**
 * @author congbai
 * @date 03/06/2018
 */
public class RedisSinkDescriptor extends SinkDescriptor {

    private String name;

    private int readMode = ReadMode.CODE.getMode();

    private Sentinel sentinel;

    private Codis codis;

    private JedisPoolConfig config;

    private int database;

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

    public Sentinel getSentinel() {
        return sentinel;
    }

    public void setSentinel(Sentinel sentinel) {
        this.sentinel = sentinel;
    }

    public Codis getCodis() {
        return codis;
    }

    public void setCodis(Codis codis) {
        this.codis = codis;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public JedisPoolConfig getConfig() {
        return config;
    }

    public void setConfig(JedisPoolConfig config) {
        this.config = config;
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
        RedisProperties redisProperties = new RedisProperties();
        BeanUtils.copyProperties(this, redisProperties);
        if (ReadMode.CODE.getMode() == this.readMode) {
            return (T)new RedisTableSink(redisProperties, this.value);
        } else {
            RedisInvoker redisInvoker = (RedisInvoker)Class.forName(this.value).newInstance();
            return (T)new RedisTableSink(redisProperties, redisInvoker);
        }
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(database, "redis的database不能为空");
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
