package com.dfire.platform.alchemy.web.descriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.dfire.platform.alchemy.web.bind.BindPropertiesFactory;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.util.DescriptorUtils;
import com.dfire.platform.alchemy.web.util.PropertiesUtils;

/**
 * 提交sql job的基本信息
 *
 * @author congbai
 * @date 01/06/2018
 */
@Component
public class TableDescriptor implements Descriptor {

    public List<SourceDescriptor> sources;
    public List<UdfDescriptor> udfs;
    public volatile List<SinkDescriptor> sinkDescriptors;
    private String jarPath;
    private Integer parallelism;
    private Long checkpointingInterval;
    private String timeCharacteristic;
    private Integer restartAttempts;
    private Long delayBetweenAttempts;
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
                List<SinkDescriptor> sinkDescriptorList = new ArrayList<>(this.sinkDescriptors.size());
                for (Map<String, Object> sink : sinks) {
                    Object type = sink.get(Constants.DESCRIPTOR_TYPE_KEY);
                    if (type == null) {
                        continue;
                    }
                    SinkDescriptor descriptor = DescriptorUtils.find(String.valueOf(type), SinkDescriptor.class);
                    if (descriptor == null) {
                        continue;
                    }
                    try {
                        SinkDescriptor sinkDescriptor = descriptor.getClass().newInstance();
                        BindPropertiesFactory.bindPropertiesToTarget(sinkDescriptor, "",
                            PropertiesUtils.createProperties(sink));
                        sinkDescriptorList.add(sinkDescriptor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return sinkDescriptorList;
            }
        }
        return sinkDescriptors;
    }

    public void setSinkDescriptors(List<SinkDescriptor> sinkDescriptors) {
        this.sinkDescriptors = sinkDescriptors;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
    }

    public Long getCheckpointingInterval() {
        return checkpointingInterval;
    }

    public void setCheckpointingInterval(Long checkpointingInterval) {
        this.checkpointingInterval = checkpointingInterval;
    }

    public String getTimeCharacteristic() {
        return timeCharacteristic;
    }

    public void setTimeCharacteristic(String timeCharacteristic) {
        this.timeCharacteristic = timeCharacteristic;
    }

    public Integer getRestartAttempts() {
        return restartAttempts;
    }

    public void setRestartAttempts(Integer restartAttempts) {
        this.restartAttempts = restartAttempts;
    }

    public Long getDelayBetweenAttempts() {
        return delayBetweenAttempts;
    }

    public void setDelayBetweenAttempts(Long delayBetweenAttempts) {
        this.delayBetweenAttempts = delayBetweenAttempts;
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
        //// TODO: 2018/6/8
    }
}
