package com.dfire.platform.web.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.dfire.platform.web.cluster.request.Request;
import com.dfire.platform.web.cluster.response.Response;
import com.dfire.platform.web.common.ClusterType;
import com.dfire.platform.web.common.ResultMessage;

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

    public ClusterManager(ClusterProperties clusterProperties, Cluster... clusters) {
        this.clusterProperties = clusterProperties;
        this.nameClusters = init(clusterProperties, clusters);
    }

    private Map<String, Cluster> init(ClusterProperties clusterProperties, Cluster[] clusters) {
        List<ClusterInfo> clusterInfos = clusterProperties.getClusters();
        Map<String, Cluster> nameClusters = new HashMap<>(clusters.length);
        for (Cluster cluster : clusters) {
            startCluster(cluster, clusterInfos);
            nameClusters.put(cluster.name(), cluster);
        }
        return nameClusters;
    }

    private void startCluster(Cluster cluster, List<ClusterInfo> clusterInfos) {
        for (ClusterInfo clusterInfo : clusterInfos) {
            if (StringUtils.equals(cluster.name(), clusterInfo.getName())) {
                cluster.start(clusterInfo);
                continue;
            }
        }
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
