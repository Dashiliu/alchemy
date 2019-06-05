package com.dfire.platform.alchemy.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.flink.runtime.jobmanager.HighAvailabilityMode;

/**
 * @author congbai
 * @date 01/06/2018
 */
public class RestInfo {

    private String mode = HighAvailabilityMode.NONE.toString().toLowerCase();

    private String clusterId;

    private String zookeeperQuorum;

    private String storagePath;

    private String address;

    private String lookupTimeout;

    private Integer port;

    private List<String> avgs;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getZookeeperQuorum() {
        return zookeeperQuorum;
    }

    public void setZookeeperQuorum(String zookeeperQuorum) {
        this.zookeeperQuorum = zookeeperQuorum;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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

    public String getLookupTimeout() {
        return lookupTimeout;
    }

    public void setLookupTimeout(String lookupTimeout) {
        this.lookupTimeout = lookupTimeout;
    }
}
