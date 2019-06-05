package com.dfire.platform.alchemy.handle.request;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class CancelFlinkRequest implements FlinkRequest {

    private String jobID;

    private Long clusterId;

    private Boolean savePoint;

    private String savepointDirectory;

    private boolean test;

    public CancelFlinkRequest(String jobID, Long clusterId) {
        this.jobID = jobID;
        this.clusterId = clusterId;
    }

    public CancelFlinkRequest(String jobID, Long clusterId, Boolean savePoint, String savepointDirectory) {
        this.jobID = jobID;
        this.clusterId = clusterId;
        this.savePoint = savePoint;
        this.savepointDirectory = savepointDirectory;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Boolean getSavePoint() {
        return savePoint;
    }

    public void setSavePoint(Boolean savePoint) {
        this.savePoint = savePoint;
    }

    public String getSavepointDirectory() {
        return savepointDirectory;
    }

    public void setSavepointDirectory(String savepointDirectory) {
        this.savepointDirectory = savepointDirectory;
    }

    @Override
    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
