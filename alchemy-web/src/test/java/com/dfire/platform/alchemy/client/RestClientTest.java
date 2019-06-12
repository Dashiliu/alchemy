package com.dfire.platform.alchemy.client;

import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.StandaloneClusterId;
import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.runtime.jobgraph.JobStatus;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class RestClientTest {

    @Test
    public void testConnection() throws Exception {
        final Configuration config = new Configuration();
        config.setString(JobManagerOptions.ADDRESS, "10.1.21.95");
        config.setInteger(RestOptions.RETRY_MAX_ATTEMPTS, 10);
        config.setLong(RestOptions.RETRY_DELAY, 0);
        RestClusterClient restClusterClient = new RestClusterClient<>(
            config,
            "stest");
        Object clusterId = restClusterClient.getClusterId();
        CompletableFuture<JobStatus>  status = restClusterClient.getJobStatus(JobID.fromHexString("7b28497575ca309bdc241b15f34155ab"));

//        String path = restClusterClient.cancelWithSavepoint(JobID.fromHexString("7b28497575ca309bdc241b15f34155ab"), "/tmp");
    }

}
