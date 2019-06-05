package com.dfire.platform.alchemy.common;

import java.util.Date;

/**
 * @author congbai
 * @date 2018/7/1
 */
public class JarInfo {

    private String avg;

    private Integer parallelism;

    private String programArgs;

    private String entryClass;

    private Date uploadTime;

    public String getAvg() {
        return avg;
    }

    public void setAvg(String avg) {
        this.avg = avg;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
    }

    public String getProgramArgs() {
        return programArgs;
    }

    public void setProgramArgs(String programArgs) {
        this.programArgs = programArgs;
    }

    public String getEntryClass() {
        return entryClass;
    }

    public void setEntryClass(String entryClass) {
        this.entryClass = entryClass;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }
}
