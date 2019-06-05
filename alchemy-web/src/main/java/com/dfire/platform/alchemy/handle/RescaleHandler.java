package com.dfire.platform.alchemy.handle;

import org.apache.flink.api.common.JobID;
import org.springframework.stereotype.Component;

import com.dfire.platform.alchemy.client.FlinkClient;
import com.dfire.platform.alchemy.handle.request.RescaleFlinkRequest;
import com.dfire.platform.alchemy.handle.response.Response;

/**
 * @author congbai
 * @date 2019/5/15
 */
@Component
public class RescaleHandler implements Handler<RescaleFlinkRequest, Response> {

    @Override
    public Response handle(FlinkClient client, RescaleFlinkRequest request) throws Exception {
        client.getClusterClient().rescaleJob(JobID.fromHexString(request.getJobID()), request.getNewParallelism());
        return new Response(true);
    }
}
