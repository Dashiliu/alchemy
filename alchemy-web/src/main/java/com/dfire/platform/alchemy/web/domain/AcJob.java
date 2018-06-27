package com.dfire.platform.alchemy.web.domain;

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
    private Integer submitMode;

    @Column(name = "status")
    private Integer status;

    @Column(name = "is_valid")
    private Integer isValid;

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

    public Integer getSubmitMode() {
        return submitMode;
    }

    public void setSubmitMode(Integer submitMode) {
        this.submitMode = submitMode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
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
