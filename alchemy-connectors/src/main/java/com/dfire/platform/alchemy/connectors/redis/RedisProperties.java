package com.dfire.platform.alchemy.connectors.redis;

import java.io.Serializable;

import redis.clients.jedis.JedisPoolConfig;

/**
 * @author congbai
 * @date 2018/7/12
 */
public class RedisProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private Codis codis;

    private Sentinel sentinel;

    private int database;

    private JedisPoolConfig config;

    private Integer queueSize;

    private Integer threadNum;

    public Codis getCodis() {
        return codis;
    }

    public void setCodis(Codis codis) {
        this.codis = codis;
    }

    public Sentinel getSentinel() {
        return sentinel;
    }

    public void setSentinel(Sentinel sentinel) {
        this.sentinel = sentinel;
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
}
