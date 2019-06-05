package com.dfire.platform.alchemy.handle.request;

/**
 * @author congbai
 * @date 01/06/2018
 */
public abstract class AbstractSubmitRequest implements FlinkRequest, SubmitRequest {

    private Long clusterId;

    private String jobName;

    private boolean test;

    @Override
    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
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
