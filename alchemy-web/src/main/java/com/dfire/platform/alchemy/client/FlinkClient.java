package com.dfire.platform.alchemy.client;

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
 * @date 2019/6/4
 */
public interface FlinkClient {

    Response cancel(CancelFlinkRequest request) throws Exception;

    Response rescale(RescaleFlinkRequest request) throws Exception;

    Response savepoint(SavepointFlinkRequest request) throws Exception;

    JobStatusResponse status(JobStatusRequest request) throws Exception;

    SubmitFlinkResponse submit(SubmitRequest request) throws Exception;

}
