package com.dfire.platform.alchemy.handle.request;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class JobStatusRequest implements StatusRequest {

    private Long clusterId;

    private String jobID;

    private boolean test;

    public JobStatusRequest(Long cluster, String jobID) {
        this.clusterId = clusterId;
        this.jobID = jobID;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    @Override
    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
