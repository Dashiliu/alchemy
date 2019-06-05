package com.dfire.platform.alchemy.handle;

import org.apache.flink.api.common.JobID;
import org.springframework.stereotype.Component;

import com.dfire.platform.alchemy.client.FlinkClient;
import com.dfire.platform.alchemy.handle.request.CancelFlinkRequest;
import com.dfire.platform.alchemy.handle.response.Response;

/**
 * @author congbai
 * @date 2019/5/15
 */
@Component
public class CancelHandler implements Handler<CancelFlinkRequest, Response> {

    @Override
    public Response handle(FlinkClient client, CancelFlinkRequest request) throws Exception {
        boolean savePoint = request.getSavePoint() !=null && request.getSavePoint().booleanValue();
        if(savePoint){
            client.getClusterClient().cancelWithSavepoint(JobID.fromHexString(request.getJobID()), request.getSavepointDirectory());
        }else{
            client.getClusterClient().cancel(JobID.fromHexString(request.getJobID()));
        }

        return new Response(true);
    }
}
