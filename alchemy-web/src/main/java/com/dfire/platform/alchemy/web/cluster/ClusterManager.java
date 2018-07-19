package com.dfire.platform.alchemy.web.cluster;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.dfire.platform.alchemy.web.cluster.request.Request;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.ResultMessage;

/**
 * @author congbai
 * @date 04/06/2018
 */
@EnableConfigurationProperties(ClusterProperties.class)
@Component
public class ClusterManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterManager.class);

    private final ClusterProperties clusterProperties;

    private final Map<String, Cluster> nameClusters;

    public ClusterManager(ClusterProperties clusterProperties) {
        this.clusterProperties = clusterProperties;
        this.nameClusters = init(this.clusterProperties);
    }

    private Map<String, Cluster> init(ClusterProperties clusterProperties) {
        List<ClusterInfo> clusterInfos = clusterProperties.getClusters();
        Map<String, Cluster> nameClusters = new HashMap<>(clusterInfos.size());
        ServiceLoader<Cluster> serviceLoader = ServiceLoader.load(Cluster.class);
        Iterator<Cluster> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            Cluster cluster = iterator.next();
            clusterInfos.forEach(clusterInfo -> {
                if (cluster.clusterType().getType().equals(clusterInfo.getType())) {
                    cluster.start(clusterInfo);
                    nameClusters.put(clusterInfo.getName(), cluster);
                }
            });
        }
        return nameClusters;
    }

    public Response send(Request message) {
        Cluster cluster = nameClusters.get(message.getCluster());
        if (cluster == null) {
            return new Response(ResultMessage.CLUSTER_NOT_EXIST.getMsg());
        }
        try {
            return cluster.send(message);
        } catch (Exception e) {
            LOGGER.error("cluster send message fail", e);
            return new Response(e.getMessage());
        }
    }

    public ClusterType getClusterType(String clusterName) {
        Cluster cluster = nameClusters.get(clusterName);
        return cluster == null ? null : cluster.clusterType();
    }

    public Set<String> clusters() {
        return nameClusters.keySet();
    }

}
