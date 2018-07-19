package com.dfire.platform.alchemy.connectors.hbase;

import java.io.Serializable;

/**
 * @author congbai
 * @date 2018/7/12
 */
public class HbaseProperties implements Serializable{

    private static final long serialVersionUID = -5600020364848439482L;

    private  String zookeeper;

    private  String node;

    private  String tableName;

    private  String family;

    private  long bufferSize;

    private  boolean skipWal;

    public String getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(String zookeeper) {
        this.zookeeper = zookeeper;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public long getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(long bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isSkipWal() {
        return skipWal;
    }

    public void setSkipWal(boolean skipWal) {
        this.skipWal = skipWal;
    }
}
