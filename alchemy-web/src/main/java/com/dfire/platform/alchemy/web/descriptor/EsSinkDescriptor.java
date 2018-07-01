package com.dfire.platform.alchemy.web.descriptor;

import org.springframework.util.Assert;

import com.dfire.platform.alchemy.connectors.elasticsearch.ElasticsearchTableSink;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;

/**
 * @author congbai
 * @date 03/06/2018
 */
public class EsSinkDescriptor extends SinkDescriptor {

    private String name;

    private String address;

    private String clusterName;

    private String index;

    private int bufferSize;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
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
        Assert.notNull(address, "地址不能为空");
        Assert.notNull(clusterName, "clusterName不能为空");
        Assert.notNull(index, "索引不能为空");
    }

    private <T> T transformFlink() {
        return (T)new ElasticsearchTableSink(this.address, this.clusterName, this.index, this.bufferSize);
    }

    @Override
    public String getType() {
        return Constants.SINK_TYPE_VALUE_ES;
    }
}
