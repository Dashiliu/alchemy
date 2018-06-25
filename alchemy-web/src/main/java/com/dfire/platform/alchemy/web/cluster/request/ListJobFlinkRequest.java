package com.dfire.platform.alchemy.web.cluster.request;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class ListJobFlinkRequest implements FlinkRequest, ListRequest {

    private String cluster;

    private boolean test;

    public ListJobFlinkRequest(String cluster) {
        this.cluster = cluster;
    }

    @Override
    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    @Override
    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
