package com.dfire.platform.alchemy.handle;

import java.util.concurrent.CompletableFuture;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.jobgraph.JobStatus;
import org.springframework.stereotype.Component;

import com.dfire.platform.alchemy.client.FlinkClient;
import com.dfire.platform.alchemy.handle.request.JobStatusRequest;
import com.dfire.platform.alchemy.handle.response.JobStatusResponse;

/**
 * @author congbai
 * @date 2019/5/15
 */
@Component
public class StatusHandler implements Handler<JobStatusRequest, JobStatusResponse> {

    @Override
    public JobStatusResponse handle(FlinkClient client, JobStatusRequest request) throws Exception {
        CompletableFuture<JobStatus> jobStatusCompletableFuture
            = client.getClusterClient().getJobStatus(JobID.fromHexString(request.getJobID()));
        // jobStatusCompletableFuture.
        switch (jobStatusCompletableFuture.get()) {
            case CREATED:
                return new JobStatusResponse(true, com.dfire.platform.alchemy.domain.enumeration.JobStatus.SUBMIT);
            case RESTARTING:
                break;
            case RUNNING:
                return new JobStatusResponse(true, com.dfire.platform.alchemy.domain.enumeration.JobStatus.RUNNING);
            case FAILING:
            case FAILED:
                return new JobStatusResponse(true, com.dfire.platform.alchemy.domain.enumeration.JobStatus.FAILED);
            case CANCELLING:
            case CANCELED:
                return new JobStatusResponse(true, com.dfire.platform.alchemy.domain.enumeration.JobStatus.CANCELED);
            case FINISHED:
                return new JobStatusResponse(true, com.dfire.platform.alchemy.domain.enumeration.JobStatus.FINISHED);
            case SUSPENDED:
            case RECONCILING:
            default:
                // nothing to do
        }
        return new JobStatusResponse(null);
    }
}
