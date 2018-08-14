package com.dfire.platform.alchemy.connectors.tsdb;

import java.io.Serializable;

/**
 * @author congbai
 * @date 2018/7/12
 */
public class OpentsdbProperties implements Serializable{

    private static final long serialVersionUID = 1L;

    private String opentsdbUrl;

    private String env;

    private Integer ioThreadCount;

    private Integer batchPutBufferSize;

    private Integer batchPutConsumerThreadCount;

    private Integer batchPutSize;

    private Integer batchPutTimeLimit;

    private Integer putRequestLimit;

    public String getOpentsdbUrl() {
        return opentsdbUrl;
    }

    public void setOpentsdbUrl(String opentsdbUrl) {
        this.opentsdbUrl = opentsdbUrl;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
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
}
