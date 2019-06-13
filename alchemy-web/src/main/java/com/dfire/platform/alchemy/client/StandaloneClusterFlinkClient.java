package com.dfire.platform.alchemy.client;

import com.dfire.platform.alchemy.client.request.*;
import com.dfire.platform.alchemy.client.response.JobStatusResponse;
import com.dfire.platform.alchemy.client.response.Response;
import com.dfire.platform.alchemy.client.response.SavepointResponse;
import com.dfire.platform.alchemy.client.response.SubmitFlinkResponse;
import org.apache.flink.client.program.ClusterClient;

import java.util.List;

/**
 * @author congbai
 * @date 2019/6/10
 */
public class StandaloneClusterFlinkClient extends AbstractFlinkClient {

    private final ClusterClient clusterClient;

    public StandaloneClusterFlinkClient(ClusterClient clusterClient, List<String> avgs) {
        super(avgs);
        this.clusterClient = clusterClient;
    }

    @Override
    public SavepointResponse cancel(CancelFlinkRequest request) throws Exception {
        return cancel(clusterClient, request);
    }

    @Override
    public Response rescale(RescaleFlinkRequest request) throws Exception {
        return rescale(clusterClient, request);
    }

    @Override
    public SavepointResponse savepoint(SavepointFlinkRequest request) throws Exception {
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
