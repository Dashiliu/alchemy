package com.dfire.platform.alchemy.web.cluster.request;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class JobStatusRequest implements StatusRequest {

    private String cluster;

    private String jobID;

    private boolean test;

    public JobStatusRequest(String cluster, String jobID) {
        this.cluster = cluster;
        this.jobID = jobID;
    }

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
