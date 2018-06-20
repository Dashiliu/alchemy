package com.dfire.platform.web.service.dto;

import java.util.Date;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class JobDTO {

    private String id;

    private String acServiceId;

    private String name;

    private int submitMode;

    private String jobId;

    private String jobName;

    private int status;

    private Date createTime;

    private Date updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAcServiceId() {
        return acServiceId;
    }

    public void setAcServiceId(String acServiceId) {
        this.acServiceId = acServiceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSubmitMode() {
        return submitMode;
    }

    public void setSubmitMode(int submitMode) {
        this.submitMode = submitMode;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
