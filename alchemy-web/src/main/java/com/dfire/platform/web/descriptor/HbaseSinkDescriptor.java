package com.dfire.platform.web.descriptor;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.dfire.platform.api.sink.HbaseInvoker;
import com.dfire.platform.connectors.hbase.HbaseTableSink;
import com.dfire.platform.web.common.ClusterType;
import com.dfire.platform.web.common.ReadMode;

/**
 * @author congbai
 * @date 03/06/2018
 */
@Component
public class HbaseSinkDescriptor extends SinkDescriptor {

    private int readMode = ReadMode.CODE.getMode();

    private String zookeeper;

    private String node;

    private String tableName;

    private String family;

    private long bufferSize;

    private String value;

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
    public String getContentType() {
        return "hbaseSink";
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        if (ReadMode.CODE.equals(this.readMode)) {
            return (T)new HbaseTableSink(this.zookeeper, this.node, this.tableName, this.family, this.bufferSize,
                this.value);
        } else {
            HbaseInvoker hbaseInvoker = (HbaseInvoker)Class.forName(this.value).newInstance();
            return (T)new HbaseTableSink(this.zookeeper, this.node, this.tableName, this.family, this.bufferSize,
                hbaseInvoker);
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
}
