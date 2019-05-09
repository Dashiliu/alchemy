package com.dfire.platform.alchemy.web.descriptor;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.connectors.elasticsearch.ElasticsearchTableSink;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;

import java.util.Map;

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

    private Long flushInterval;

    private String dateFormat;

    /**
     *  索引从字段里取
     */
    private String filedIndex;

    private Map<String, Object> properties;

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

    public Long getFlushInterval() {
        return flushInterval;
    }

    public void setFlushInterval(Long flushInterval) {
        this.flushInterval = flushInterval;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
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
        if (StringUtils.isBlank(index) && StringUtils.isBlank(filedIndex)){
            throw new IllegalArgumentException("索引不能为空");
        }
    }

    private <T> T transformFlink() {
        return (T)new ElasticsearchTableSink(this.address, this.clusterName, this.index, this.bufferSize, this.flushInterval ,this.filedIndex,this.dateFormat,this.properties);
    }

    @Override
    public String getType() {
        return Constants.SINK_TYPE_VALUE_ES;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getFiledIndex() {
        return filedIndex;
    }

    public void setFiledIndex(String filedIndex) {
        this.filedIndex = filedIndex;
    }
}
