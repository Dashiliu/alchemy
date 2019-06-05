package com.dfire.platform.alchemy.connectors.elasticsearch;

import java.io.Serializable;
import java.util.Map;

/**
 * @author congbai
 * @date 2019/6/2
 */
public class ElasticsearchProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private  String transports;

    private  String clusterName;

    private  String index;

    private  String fieldIndex;

    private  String dateFormat;

    private  Map<String, Object> config;

    public String getTransports() {
        return transports;
    }

    public void setTransports(String transports) {
        this.transports = transports;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getFieldIndex() {
        return fieldIndex;
    }

    public void setFieldIndex(String fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}
