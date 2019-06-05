package com.dfire.platform.alchemy.handle;

import java.util.concurrent.CompletableFuture;

import org.apache.flink.api.common.JobID;
import org.springframework.stereotype.Component;

import com.dfire.platform.alchemy.client.FlinkClient;
import com.dfire.platform.alchemy.handle.request.SavepointFlinkRequest;
import com.dfire.platform.alchemy.handle.response.Response;

/**
 * @author congbai
 * @date 2019/5/15
 */
@Component
public class SavepointHandler implements Handler<SavepointFlinkRequest, Response> {

    @Override
    public Response handle(FlinkClient client, SavepointFlinkRequest request) throws Exception {
        CompletableFuture<String> future = client.getClusterClient()
            .triggerSavepoint(JobID.fromHexString(request.getJobID()), request.getSavepointDirectory());
        return new Response(true, future.get());
    }
}
