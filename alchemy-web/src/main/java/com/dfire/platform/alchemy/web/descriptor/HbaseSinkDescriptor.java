package com.dfire.platform.alchemy.web.descriptor;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.api.sink.HbaseInvoker;
import com.dfire.platform.alchemy.connectors.hbase.HbaseProperties;
import com.dfire.platform.alchemy.connectors.hbase.HbaseTableSink;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.common.ReadMode;

/**
 * @author congbai
 * @date 03/06/2018
 */
public class HbaseSinkDescriptor extends SinkDescriptor {

    private String name;

    private int readMode = ReadMode.CODE.getMode();

    private String zookeeper;

    private String node;

    private String tableName;

    private String family;

    private long bufferSize;

    private boolean skipWal;

    private String value;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
        if (ClusterType.FLINK.equals(clusterType)) {
            return transformFlink();
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);
    }

    private <T> T transformFlink() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        HbaseProperties hbaseProperties = new HbaseProperties();
        BeanUtils.copyProperties(this, hbaseProperties);
        if (ReadMode.CODE.getMode() == this.readMode) {
            return (T)new HbaseTableSink(hbaseProperties, this.value);
        } else {
            HbaseInvoker hbaseInvoker = (HbaseInvoker)Class.forName(this.value).newInstance();
            return (T)new HbaseTableSink(hbaseProperties, hbaseInvoker);
        }
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(zookeeper, "hbase的zookeeper地址不能为空");
        Assert.notNull(node, "hbase在zookeeper的根目录不能为空");
        Assert.notNull(tableName, "hbase的表名不能为空");
        Assert.notNull(family, "hbase的family不能为空");
        Assert.notNull(value, "hbase的获取rowKey和column的逻辑不能为空");
    }

    @Override
    public String getType() {
        return Constants.SINK_TYPE_VALUE_HBASE;
    }
}
