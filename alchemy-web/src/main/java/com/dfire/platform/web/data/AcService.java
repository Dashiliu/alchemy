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
public class AcService {

    @Id
    private Long id;

    @Column(name = "relation_id")
    private Long relationId;

    @Column(name = "name")
    private String name;

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

    public Long getRelationId() {
        return relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
