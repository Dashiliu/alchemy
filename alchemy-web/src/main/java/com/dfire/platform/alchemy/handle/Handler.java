package com.dfire.platform.alchemy.handle;

import com.dfire.platform.alchemy.client.FlinkClient;
import com.dfire.platform.alchemy.handle.request.Request;
import com.dfire.platform.alchemy.handle.response.Response;

/**
 * @author congbai
 * @date 2019/5/15
 */
public interface Handler<T extends Request, R extends Response> {

    R handle(FlinkClient client, T request) throws Exception;

}
