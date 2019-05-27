package com.dfire.platform.alchemy.web.descriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.dfire.platform.alchemy.web.util.BindPropertiesUtils;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.util.PropertiesUtils;

/**
 * 提交sql job的基本信息
 *
 * @author congbai
 * @date 01/06/2018
 */
public class TableDescriptor implements Descriptor {

    public List<SourceDescriptor> sources;
    public List<UdfDescriptor> udfs;
    public volatile List<SinkDescriptor> sinkDescriptors;
    private String sql;
    private List<String> codes;
    private List<Map<String, Object>> sinks;

    public List<UdfDescriptor> getUdfs() {
        return udfs;
    }

    public void setUdfs(List<UdfDescriptor> udfs) {
        this.udfs = udfs;
    }

    public List<SourceDescriptor> getSources() {
        return sources;
    }

    public void setSources(List<SourceDescriptor> sources) {
        this.sources = sources;
    }

    public List<Map<String, Object>> getSinks() {
        return sinks;
    }

    public void setSinks(List<Map<String, Object>> sinks) {
        this.sinks = sinks;
    }

    public List<SinkDescriptor> getSinkDescriptors() {
        if (this.sinkDescriptors == null) {
            synchronized (this) {
                if (CollectionUtils.isEmpty(this.sinks)) {
                    return this.sinkDescriptors;
                }
                List<SinkDescriptor> sinkDescriptorList = new ArrayList<>(this.sinks.size());
                for (Map<String, Object> sink : sinks) {
                    Object type = sink.get(Constants.DESCRIPTOR_TYPE_KEY);
                    if (type == null) {
                        continue;
                    }
                    SinkDescriptor descriptor
                        = DescriptorFactory.me.find(String.valueOf(type), SinkDescriptor.class);
                    if (descriptor == null) {
                        continue;
                    }
                    try {
                        SinkDescriptor sinkDescriptor = descriptor.getClass().newInstance();
                        BeanUtils.populate(sinkDescriptor, sink);
                        sinkDescriptorList.add(sinkDescriptor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                this.sinkDescriptors = sinkDescriptorList;
            }
        }
        return sinkDescriptors;
    }

    public void setSinkDescriptors(List<SinkDescriptor> sinkDescriptors) {
        this.sinkDescriptors = sinkDescriptors;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    @Override
    public String getType() {
        return Constants.TYPE_VALUE_TABLE;
    }

    @Override
    public void validate() throws Exception {
        Assert.notEmpty(sources, "source不能为空");
        Assert.notEmpty(getSinkDescriptors(), "sink不能为空");
        for (SourceDescriptor sourceDescriptor : sources) {
            sourceDescriptor.validate();
        }
        for (SinkDescriptor sinkDescriptor : getSinkDescriptors()) {
            sinkDescriptor.validate();
        }
        if (CollectionUtils.isEmpty(udfs)) {
            return;
        }
        for (UdfDescriptor udfDescriptor : udfs) {
            udfDescriptor.validate();
        }
    }
}
