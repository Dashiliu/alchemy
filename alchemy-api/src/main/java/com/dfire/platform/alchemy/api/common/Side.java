package com.dfire.platform.alchemy.api.common;

import java.io.Serializable;

/**
 * @author congbai
 * @date 2019/5/16
 */
public class Side implements Serializable {

    private static final long serialVersionUID = 1;

    private String cacheType;

    private int cacheSize = 10000;

    private long ttl = 60 * 1000;

    /**
     * The given timeout
     */
    private long timeout = 10000;

    /**
     * The max number of async i/o operation that can be triggered
     */
    private int capacity = Runtime.getRuntime().availableProcessors() * 2;

    private boolean partition;

    private boolean async;

    private boolean logTimeoutOnly;

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isPartition() {
        return partition;
    }

    public void setPartition(boolean partition) {
        this.partition = partition;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isLogTimeoutOnly() {
        return logTimeoutOnly;
    }

    public void setLogTimeoutOnly(boolean logTimeoutOnly) {
        this.logTimeoutOnly = logTimeoutOnly;
    }
}
