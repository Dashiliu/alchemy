package com.dfire.platform.web.cluster.request;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class CancelFlinkRequest implements FlinkRequest {

    private String jobID;

    private String cluster;

    private boolean test;

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    @Override
    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    @Override
    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
