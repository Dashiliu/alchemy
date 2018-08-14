package com.dfire.platform.alchemy.web.descriptor;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.connectors.tsdb.OpentsdbProperties;
import com.dfire.platform.alchemy.connectors.tsdb.OpentsdbTableSink;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;

/**
 * @author congbai
 * @date 03/06/2018
 */
public class TsdbSinkDescriptor extends SinkDescriptor {

    private String name;

    private String opentsdbUrl;

    private Integer ioThreadCount;

    private Integer batchPutBufferSize;

    private Integer batchPutConsumerThreadCount;

    private Integer batchPutSize;

    private Integer batchPutTimeLimit;

    private Integer putRequestLimit;

    private String value;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        if (ClusterType.FLINK.equals(clusterType)) {
            return transformFlink();
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);

    }

    private <T> T transformFlink() throws Exception {
        OpentsdbProperties opentsdbProperties = new OpentsdbProperties();
        BeanUtils.copyProperties(this, opentsdbProperties);
        return (T)new OpentsdbTableSink(opentsdbProperties, this.value);
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(opentsdbUrl, "opentsdbUrl不能为空");
        Assert.notNull(value, "写入opentsdb的逻辑不能为空");
    }

    public String getOpentsdbUrl() {
        return opentsdbUrl;
    }

    public void setOpentsdbUrl(String opentsdbUrl) {
        this.opentsdbUrl = opentsdbUrl;
    }

    public Integer getIoThreadCount() {
        return ioThreadCount;
    }

    public void setIoThreadCount(Integer ioThreadCount) {
        this.ioThreadCount = ioThreadCount;
    }

    public Integer getBatchPutBufferSize() {
        return batchPutBufferSize;
    }

    public void setBatchPutBufferSize(Integer batchPutBufferSize) {
        this.batchPutBufferSize = batchPutBufferSize;
    }

    public Integer getBatchPutConsumerThreadCount() {
        return batchPutConsumerThreadCount;
    }

    public void setBatchPutConsumerThreadCount(Integer batchPutConsumerThreadCount) {
        this.batchPutConsumerThreadCount = batchPutConsumerThreadCount;
    }

    public Integer getBatchPutSize() {
        return batchPutSize;
    }

    public void setBatchPutSize(Integer batchPutSize) {
        this.batchPutSize = batchPutSize;
    }

    public Integer getBatchPutTimeLimit() {
        return batchPutTimeLimit;
    }

    public void setBatchPutTimeLimit(Integer batchPutTimeLimit) {
        this.batchPutTimeLimit = batchPutTimeLimit;
    }

    public Integer getPutRequestLimit() {
        return putRequestLimit;
    }

    public void setPutRequestLimit(Integer putRequestLimit) {
        this.putRequestLimit = putRequestLimit;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return Constants.SINK_TYPE_VALUE_OPENTSDB;
    }
}
