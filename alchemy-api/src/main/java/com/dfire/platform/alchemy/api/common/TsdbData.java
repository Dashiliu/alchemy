package com.dfire.platform.alchemy.api.common;

import java.util.Map;

/**
 * @author congbai
 * @date 2018/7/10
 */
public class TsdbData {

    private Map<String,Number> metricValues;

    private Map<String,String> tags;

    public Map<String, Number> getMetricValues() {
        return metricValues;
    }

    public void setMetricValues(Map<String, Number> metricValues) {
        this.metricValues = metricValues;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public static class Builder{

        private Map<String,Number> metricValues;

        private Map<String,String> tags;


    }

}
