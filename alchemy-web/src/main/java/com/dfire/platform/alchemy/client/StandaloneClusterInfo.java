package com.dfire.platform.alchemy.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.flink.runtime.jobmanager.HighAvailabilityMode;

/**
 * @author congbai
 * @date 01/06/2018
 */
public class StandaloneClusterInfo {

    private String clusterId;

    private Map<String, Object> configuration;

    private List<String> avgs;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public List<String> getAvgs() {
        return avgs;
    }

    public void setAvgs(List<String> avgs) {
        this.avgs = avgs;
    }

    public void setAvg(String avg) {
        List<String> avgs = new ArrayList<>(1);
        avgs.add(avg);
        this.avgs = avgs;
    }
}
