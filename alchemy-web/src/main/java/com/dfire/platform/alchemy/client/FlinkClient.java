package com.dfire.platform.alchemy.client;

import java.util.List;

import org.apache.flink.client.program.ClusterClient;

/**
 * @author congbai
 * @date 2019/6/4
 */
public class FlinkClient {

    private final ClusterClient clusterClient;

    /**
     * 集群额外依赖的公共包
     */
    private final List<String> avgs;

    public FlinkClient(ClusterClient clusterClient, List<String> avgs) {
        this.clusterClient = clusterClient;
        this.avgs = avgs;
    }

    public ClusterClient getClusterClient() {
        return clusterClient;
    }

    public List<String> getAvgs() {
        return avgs;
    }
}
