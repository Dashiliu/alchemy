package com.dfire.platform.web.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author congbai
 * @date 11/11/2017
 */
@Entity
@Table(name = "ac_job")
public class AcJob {

    @Id
    private Long id;

    @Column(name = "ac_service_id")
    private Long acServiceId;

    @Column(name = "name")
    private String name;

    @Column(name = "cluster")
    private String cluster;

    @Column(name = "submit_mode")
    private int submitMode;

    @Column(name = "status")
    private int status;

    @Column(name = "is_valid")
    private int isValid;

    @Column(name = "create_time")
    private Date createTime = null;

    @Column(name = "update_time")
    private Date updateTime = null;

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

    public int getIsValid() {
        return isValid;
    }

    public void setIsValid(int isValid) {
        this.isValid = isValid;
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
