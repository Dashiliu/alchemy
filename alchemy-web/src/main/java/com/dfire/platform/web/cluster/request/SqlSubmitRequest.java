package com.dfire.platform.web.cluster.request;

import java.util.List;

import com.dfire.platform.web.descriptor.SinkDescriptor;
import com.dfire.platform.web.descriptor.SourceDescriptor;
import com.dfire.platform.web.descriptor.UdfDescriptor;

/**
 * @author congbai
 * @date 04/06/2018
 */
public class SqlSubmitRequest extends AbstractSubmitRequest {

    private String jobName;

    private Long checkpointingInterval;

    private String timeCharacteristic;

    private Integer restartAttempts;

    private Long delayBetweenAttempts;

    private String sql;

    private List<SourceDescriptor> inputs;

    private List<UdfDescriptor> userDefineFunctions;

    private List<SinkDescriptor> outputs;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
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

    public Long getCheckpointingInterval() {
        return checkpointingInterval;
    }

    public void setCheckpointingInterval(Long checkpointingInterval) {
        this.checkpointingInterval = checkpointingInterval;
    }

    public List<SourceDescriptor> getInputs() {
        return inputs;
    }

    public void setInputs(List<SourceDescriptor> inputs) {
        this.inputs = inputs;
    }

    public List<UdfDescriptor> getUserDefineFunctions() {
        return userDefineFunctions;
    }

    public void setUserDefineFunctions(List<UdfDescriptor> userDefineFunctions) {
        this.userDefineFunctions = userDefineFunctions;
    }

    public List<SinkDescriptor> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<SinkDescriptor> outputs) {
        this.outputs = outputs;
    }

    @Override
    public String getClusterName() {
        return null;
    }
}
