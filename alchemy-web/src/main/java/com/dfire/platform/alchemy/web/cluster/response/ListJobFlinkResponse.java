package com.dfire.platform.alchemy.web.cluster.response;

import java.util.Collection;

import org.apache.flink.runtime.client.JobStatusMessage;

/**
 * @author congbai
 * @date 2018/6/20
 */
public class ListJobFlinkResponse extends ListJobResponse {

    private Collection<JobStatusMessage> jobStatusMessages;

    public ListJobFlinkResponse(boolean success, Collection<JobStatusMessage> jobStatusMessages) {
        super(success);
        this.jobStatusMessages = jobStatusMessages;
    }

    public ListJobFlinkResponse(String message) {
        super(message);
    }

    public Collection<JobStatusMessage> getJobStatusMessages() {
        return jobStatusMessages;
    }

    public void setJobStatusMessages(Collection<JobStatusMessage> jobStatusMessages) {
        this.jobStatusMessages = jobStatusMessages;
    }
}
