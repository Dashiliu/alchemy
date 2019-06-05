package com.dfire.platform.alchemy.handle.request;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class RescaleFlinkRequest implements FlinkRequest {

    private String jobID;

    private Long clusterId;

    private int newParallelism;

    private boolean test;

    public RescaleFlinkRequest(String jobID, Long clusterId, int newParallelism) {
        this.jobID = jobID;
        this.clusterId = clusterId;
        this.newParallelism = newParallelism;
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

    public int getNewParallelism() {
        return newParallelism;
    }

    public void setNewParallelism(int newParallelism) {
        this.newParallelism = newParallelism;
    }

    @Override
    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
