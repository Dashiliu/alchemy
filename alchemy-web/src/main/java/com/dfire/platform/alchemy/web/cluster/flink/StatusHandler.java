package com.dfire.platform.alchemy.web.cluster.flink;

import java.util.concurrent.CompletableFuture;

import org.apache.flink.api.common.JobID;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.runtime.jobgraph.JobStatus;

import com.dfire.platform.alchemy.web.cluster.Handler;
import com.dfire.platform.alchemy.web.cluster.request.JobStatusRequest;
import com.dfire.platform.alchemy.web.cluster.response.JobStatusResponse;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.common.Status;

/**
 * @author congbai
 * @date 2019/5/15
 */
public class StatusHandler implements Handler<JobStatusRequest, Response> {

    private final ClusterClient clusterClient;

    public StatusHandler(ClusterClient clusterClient) {
        this.clusterClient = clusterClient;
    }

    @Override
    public Response handle(JobStatusRequest request) throws Exception {
        CompletableFuture<JobStatus> jobStatusCompletableFuture
            = clusterClient.getJobStatus(JobID.fromHexString(request.getJobID()));
        // jobStatusCompletableFuture.
        switch (jobStatusCompletableFuture.get()) {
            case CREATED:
            case RESTARTING:
                break;
            case RUNNING:
                return new JobStatusResponse(true, Status.RUNNING.getStatus());
            case FAILING:
            case FAILED:
                return new JobStatusResponse(true, Status.FAILED.getStatus());
            case CANCELLING:
            case CANCELED:
                return new JobStatusResponse(true, Status.CANCELED.getStatus());
            case FINISHED:
                return new JobStatusResponse(true, Status.FINISHED.getStatus());
            case SUSPENDED:
            case RECONCILING:
            default:
                // nothing to do
        }
        return new JobStatusResponse(null);
    }
}
