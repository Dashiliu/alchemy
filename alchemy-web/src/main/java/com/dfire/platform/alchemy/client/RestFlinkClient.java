package com.dfire.platform.alchemy.client;

import java.util.List;

import org.apache.flink.client.program.ClusterClient;

import com.dfire.platform.alchemy.client.request.CancelFlinkRequest;
import com.dfire.platform.alchemy.client.request.JarSubmitFlinkRequest;
import com.dfire.platform.alchemy.client.request.JobStatusRequest;
import com.dfire.platform.alchemy.client.request.RescaleFlinkRequest;
import com.dfire.platform.alchemy.client.request.SavepointFlinkRequest;
import com.dfire.platform.alchemy.client.request.SqlSubmitFlinkRequest;
import com.dfire.platform.alchemy.client.request.SubmitRequest;
import com.dfire.platform.alchemy.client.response.JobStatusResponse;
import com.dfire.platform.alchemy.client.response.Response;
import com.dfire.platform.alchemy.client.response.SubmitFlinkResponse;

/**
 * @author congbai
 * @date 2019/6/10
 */
public class RestFlinkClient extends AbstractFlinkClient {

    private final ClusterClient clusterClient;

    public RestFlinkClient(ClusterClient clusterClient, List<String> avgs) {
        super(avgs);
        this.clusterClient = clusterClient;
    }

    @Override
    public Response cancel(CancelFlinkRequest request) throws Exception {
        return cancel(clusterClient, request);
    }

    @Override
    public Response rescale(RescaleFlinkRequest request) throws Exception {
        return rescale(clusterClient, request);
    }

    @Override
    public Response savepoint(SavepointFlinkRequest request) throws Exception {
        return savepoint(clusterClient, request);
    }

    @Override
    public JobStatusResponse status(JobStatusRequest request) throws Exception {
        return status(clusterClient, request);
    }

    @Override
    public SubmitFlinkResponse submit(SubmitRequest request) throws Exception {
        if (request instanceof JarSubmitFlinkRequest) {
            return submitJar(clusterClient, (JarSubmitFlinkRequest)request);
        } else if (request instanceof SqlSubmitFlinkRequest) {
            return submitSql(clusterClient, (SqlSubmitFlinkRequest)request);
        }
        throw new UnsupportedOperationException();
    }
}
