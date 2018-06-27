package com.dfire.platform.alchemy.web.service.dto;

import java.util.Date;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class JobDTO {

    private Long id;

    private Long acServiceId;

    private String name;

    private int submitMode;

    private String cluster;

    private int status;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAcServiceId() {
        return acServiceId;
    }

    public void setAcServiceId(Long acServiceId) {
        this.acServiceId = acServiceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public int getSubmitMode() {
        return submitMode;
    }

    public void setSubmitMode(int submitMode) {
        this.submitMode = submitMode;
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
