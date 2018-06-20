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
@Table(name = "ac_service")
public class AcJobConf {

    @Id
    private Long id;

    @Column(name = "ac_job_id")
    private Long acJobId;

    @Column(name = "content")
    private String content;

    @Column(name = "type")
    private int type;

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

    public Long getAcJobId() {
        return acJobId;
    }

    public void setAcJobId(Long acJobId) {
        this.acJobId = acJobId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
