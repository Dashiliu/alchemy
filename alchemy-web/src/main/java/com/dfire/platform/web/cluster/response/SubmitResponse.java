package com.dfire.platform.web.cluster.response;

/**
 * @author congbai
 * @date 04/06/2018
 */
public class SubmitResponse extends Response {

    private String jobId;

    public SubmitResponse(String message) {
        super(message);
    }

    public SubmitResponse(boolean success, String jobId) {
        super(success);
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
