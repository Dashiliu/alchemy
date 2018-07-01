package com.dfire.platform.alchemy.web.cluster.request;

import com.dfire.platform.alchemy.web.descriptor.TableDescriptor;

/**
 * @author congbai
 * @date 04/06/2018
 */
public class SqlSubmitFlinkRequest extends AbstractSubmitRequest {

    private String jarPath;
    private Integer parallelism;
    private Long checkpointingInterval;
    private String timeCharacteristic;
    private Integer restartAttempts;
    private Long delayBetweenAttempts;

    private TableDescriptor table;

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

    public TableDescriptor getTable() {
        return table;
    }

    public void setTable(TableDescriptor table) {
        this.table = table;
    }
}
