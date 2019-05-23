package com.dfire.platform.alchemy.web.cluster.flink;

import com.dfire.platform.alchemy.web.cluster.Handler;
import org.apache.flink.client.program.ClusterClient;

import com.dfire.platform.alchemy.web.cluster.Cluster;
import com.dfire.platform.alchemy.web.cluster.ClusterInfo;
import com.dfire.platform.alchemy.web.cluster.request.Request;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.common.ClusterType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author congbai
 * @date 01/06/2018
 */
public class FlinkCluster implements Cluster {

    private ClusterClient clusterClient;

    private ClusterInfo clusterInfo;

    private Map<String, Handler> handlers = new HashMap<>();

    @Override
    public Cluster newInstance() {
        return new FlinkCluster();
    }

    @Override
    public ClusterType clusterType() {
        return ClusterType.FLINK;
    }

    @Override
    public void start(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
        this.clusterClient = ClusterClientFactory.get(clusterInfo);
        this.handlers = createHandlers(this.clusterInfo, this.clusterClient);
    }

    private Map<String, Handler> createHandlers(ClusterInfo clusterInfo, ClusterClient clusterClient) {
        //todo 优雅的自动装载所需要的handler
        CancelHandler cancelHandler =new CancelHandler(clusterClient);
        StatusHandler statusHandler =new StatusHandler(clusterClient);
        SubmitJarHandler submitJarHandler =new SubmitJarHandler(clusterClient);
        SubmitSqlHandler submitSqlHandler =new SubmitSqlHandler(clusterClient,clusterInfo);
        Map<String,Handler> handles =new HashMap<>(4);
        handles.put(cancelHandler.getClass().getName() , cancelHandler);
        handles.put(statusHandler.getClass().getName() , statusHandler);
        handles.put(submitJarHandler.getClass().getName() , submitJarHandler);
        handles.put(submitSqlHandler.getClass().getName() , submitSqlHandler);
        return handles;
    }

    @Override
    public Response send(Request message) throws Exception {
        Handler handler = handlers.get(message.getClass().getName());
        if (handler == null ){
            throw new UnsupportedOperationException("unknow message type:" + message.getClass().getName());
        }
        return handler.handle(message);
    }

    @Override
    public void destroy() throws Exception {
        clusterClient.shutdown();
    }

}
