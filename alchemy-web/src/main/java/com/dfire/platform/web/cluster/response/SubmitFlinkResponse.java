package com.dfire.platform.web.cluster.response;

/**
 * @author congbai
 * @date 04/06/2018
 */
public class SubmitFlinkResponse extends Response {

    private String jobId;

    public SubmitFlinkResponse(String message) {
        super(message);
    }

    public SubmitFlinkResponse(boolean success, String jobId) {
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
