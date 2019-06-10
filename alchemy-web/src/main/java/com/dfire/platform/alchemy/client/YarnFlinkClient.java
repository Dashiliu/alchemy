package com.dfire.platform.alchemy.client;

import org.apache.flink.configuration.Configuration;
import org.apache.hadoop.yarn.client.api.YarnClient;

import com.dfire.platform.alchemy.client.request.CancelFlinkRequest;
import com.dfire.platform.alchemy.client.request.JobStatusRequest;
import com.dfire.platform.alchemy.client.request.RescaleFlinkRequest;
import com.dfire.platform.alchemy.client.request.SavepointFlinkRequest;
import com.dfire.platform.alchemy.client.request.SubmitRequest;
import com.dfire.platform.alchemy.client.response.JobStatusResponse;
import com.dfire.platform.alchemy.client.response.Response;
import com.dfire.platform.alchemy.client.response.SubmitFlinkResponse;

/**
 * todo 支持yarn mode
 * 
 * @author congbai
 * @date 2019/6/10
 */
public class YarnFlinkClient implements FlinkClient {

    private final YarnClient yarnClient;

    private final Configuration flinkConf;

    public YarnFlinkClient(YarnClient yarnClient, Configuration flinkConf) {
        this.yarnClient = yarnClient;
        this.flinkConf = flinkConf;
    }

    @Override
    public Response cancel(CancelFlinkRequest cancelFlinkRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response rescale(RescaleFlinkRequest rescaleFlinkRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response savepoint(SavepointFlinkRequest savepointFlinkRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JobStatusResponse status(JobStatusRequest statusRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SubmitFlinkResponse submit(SubmitRequest submitRequest) {
        throw new UnsupportedOperationException();
    }
}
