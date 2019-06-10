package com.dfire.platform.alchemy.client.request;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class JobStatusRequest implements StatusRequest {

    private String jobID;

    private boolean test;

    public JobStatusRequest(String jobID) {
        this.jobID = jobID;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    @Override
    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
