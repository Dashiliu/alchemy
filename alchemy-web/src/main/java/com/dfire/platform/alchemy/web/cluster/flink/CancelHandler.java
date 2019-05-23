package com.dfire.platform.alchemy.web.cluster.flink;

import org.apache.flink.api.common.JobID;
import org.apache.flink.client.program.ClusterClient;

import com.dfire.platform.alchemy.web.cluster.Handler;
import com.dfire.platform.alchemy.web.cluster.response.Response;

/**
 * @author congbai
 * @date 2019/5/15
 */
public class CancelHandler implements Handler<CancelFlinkRequest, Response> {

    private final ClusterClient clusterClient;

    public CancelHandler(ClusterClient clusterClient) {
        this.clusterClient = clusterClient;
    }

    @Override
    public Response handle(CancelFlinkRequest request) throws Exception {
        clusterClient.cancel(JobID.fromHexString(request.getJobID()));
        return new Response(true);
    }
}
