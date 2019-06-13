package com.dfire.platform.alchemy.descriptor;

import com.dfire.platform.alchemy.common.Constants;
import com.dfire.platform.alchemy.connectors.elasticsearch.ElasticsearchProperties;
import com.dfire.platform.alchemy.connectors.elasticsearch.ElasticsearchTableSink;
import org.apache.flink.table.shaded.org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author congbai
 * @date 03/06/2018
 */
public class EsSinkDescriptor extends SinkDescriptor {

    private String name;

    private String transports;

    private String clusterName;

    private String index;

    private String fieldIndex;

    private String dateFormat;

    private Map<String, Object> config;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    @Override
    public <T> T transform() throws Exception {
        ElasticsearchProperties properties = new ElasticsearchProperties();
        BeanUtils.copyProperties(this, properties);
        return (T)new ElasticsearchTableSink(properties);
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(transports, "地址不能为空");
        Assert.notNull(clusterName, "clusterName不能为空");
        Assert.isTrue(StringUtils.isBlank(index) && StringUtils.isBlank(fieldIndex), "索引不能为空");
    }

    @Override
    public String type() {
        return Constants.SINK_TYPE_VALUE_ES;
    }

}
