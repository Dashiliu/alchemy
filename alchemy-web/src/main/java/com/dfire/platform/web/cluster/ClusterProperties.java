package com.dfire.platform.web.cluster;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author congbai
 * @date 01/06/2018
 */
@ConfigurationProperties(prefix = "stream")
public class ClusterProperties {

    private List<ClusterInfo> clusters;

    public List<ClusterInfo> getClusters() {
        return clusters;
    }

    public void setClusters(List<ClusterInfo> clusters) {
        this.clusters = clusters;
    }
}
