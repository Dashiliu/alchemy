package com.dfire.platform.web.cluster.request;

/**
 * @author congbai
 * @date 01/06/2018
 */
public abstract class AbstractSubmitRequest implements FlinkRequest {

    private String clusterName;

    private String jarPath;

    private int parallelism = 1;

    private boolean test;

    @Override
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    @Override
    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
