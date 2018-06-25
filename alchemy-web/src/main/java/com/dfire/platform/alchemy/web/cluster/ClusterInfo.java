package com.dfire.platform.alchemy.web.cluster;

import org.apache.flink.runtime.jobmanager.HighAvailabilityMode;

/**
 * @author congbai
 * @date 01/06/2018
 */
public class ClusterInfo {

    private String name;

    private String mode = HighAvailabilityMode.NONE.toString().toLowerCase();

    private String clusterId;

    private String zookeeperQuorum;

    private String storagePath;

    private String address;

    private Integer port;

    private String globalClassPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getGlobalClassPath() {
        return globalClassPath;
    }

    public void setGlobalClassPath(String globalClassPath) {
        this.globalClassPath = globalClassPath;
    }
}
