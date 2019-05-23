package com.dfire.platform.alchemy.web.cluster;

import com.dfire.platform.alchemy.web.cluster.request.Request;
import com.dfire.platform.alchemy.web.cluster.response.Response;

/**
 * @author congbai
 * @date 2019/5/15
 */
public interface Handler<T extends Request,R extends Response> {

    R handle(T request) throws Exception;

}
