package com.dfire.platform.alchemy.web.common;

/**
 * @author congbai
 * @date 05/06/2018
 */
public enum ClusterType {

    FLINK("flink");
    private String type;

    ClusterType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
