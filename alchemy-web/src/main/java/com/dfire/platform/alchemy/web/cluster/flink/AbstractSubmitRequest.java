package com.dfire.platform.alchemy.web.cluster.flink;

import com.dfire.platform.alchemy.web.cluster.request.SubmitRequest;

/**
 * @author congbai
 * @date 01/06/2018
 */
public abstract class AbstractSubmitRequest implements FlinkRequest, SubmitRequest {

    private String cluster;

    private String jobName;

    private boolean test;

    @Override
    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public abstract void validate() throws Exception;

}
