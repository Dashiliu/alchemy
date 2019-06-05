package com.dfire.platform.alchemy.handle;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dfire.platform.alchemy.client.ClientManager;
import com.dfire.platform.alchemy.client.FlinkClient;
import com.dfire.platform.alchemy.handle.request.Request;
import com.dfire.platform.alchemy.handle.response.Response;

/**
 * @author congbai
 * @date 2019/6/4
 */
@Component
public class HandlerManager {

    private final ClientManager clientManager;

    private final Map<String, Handler> handlers;

    public HandlerManager(ClientManager clientManager, Handler... handlers) {
        this.clientManager = clientManager;
        this.handlers = create(handlers);
    }

    private Map<String, Handler> create(Handler[] handlers) {
        Map<String, Handler> handlerMap = new HashMap<>(handlers.length);
        for (Handler handler : handlers) {
            Type type = handler.getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType)type;
                Type clazz = pType.getActualTypeArguments()[0];
                handlerMap.put(clazz.getTypeName(), handler);
            }
        }
        return handlerMap;
    }

    public Response send(Request request) throws Exception {
        Handler handler = handlers.get(request.getClass().getName());
        if (handler == null) {
            throw new UnsupportedOperationException("unknow message type:" + request.getClass().getName());
        }
        FlinkClient clusterClient = clientManager.getClient(request.getClusterId());
        if (clusterClient == null) {
            throw new UnsupportedOperationException("unknow message type:" + request.getClusterId());
        }
        return handler.handle(clusterClient, request);
    }
}
