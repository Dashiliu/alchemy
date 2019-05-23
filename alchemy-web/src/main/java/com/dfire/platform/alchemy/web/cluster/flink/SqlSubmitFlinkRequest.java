package com.dfire.platform.alchemy.web.cluster.flink;

import java.util.List;

import org.springframework.util.Assert;

import com.dfire.platform.alchemy.web.descriptor.TableDescriptor;

/**
 * @author congbai
 * @date 04/06/2018
 */
public class SqlSubmitFlinkRequest extends AbstractSubmitRequest {

    private List<String> avgs;
    private Integer parallelism;
    private Long checkpointingInterval;
    private String timeCharacteristic;
    private Integer restartAttempts;
    private Long delayBetweenAttempts;

    private TableDescriptor table;

    public List<String> getAvgs() {
        return avgs;
    }

    public void setAvgs(List<String> avgs) {
        this.avgs = avgs;
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

    @Override
    public void validate() throws Exception {
        Assert.notNull(parallelism, "并发数不能为空");
        Assert.notNull(table, "table不能为空");
        table.validate();
    }
}
