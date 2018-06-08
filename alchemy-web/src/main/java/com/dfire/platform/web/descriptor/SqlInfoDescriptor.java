package com.dfire.platform.web.descriptor;

/**
 * 提交sql job的基本信息
 *
 * @author congbai
 * @date 01/06/2018
 */
public class SqlInfoDescriptor extends BasicDescriptor {

    private String clusterName;

    private String jarPath;

    private Integer parallelism;

    private Long checkpointingInterval;

    private String timeCharacteristic;

    private Integer restartAttempts;

    private Long delayBetweenAttempts;

    private String sql;


    @Override
    public void validate() throws Exception {
        //// TODO: 2018/6/8
    }

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
}
