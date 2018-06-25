package com.dfire.platform.alchemy.web.service.dto;

import java.util.Date;

/**
 * @author congbai
 * @date 11/11/2017
 */
public class JobConfDTO {

    private String id;

    private String acJobId;

    private String content;

    private int type;

    private Date createTime;

    private Date updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAcJobId() {
        return acJobId;
    }

    public void setAcJobId(String acJobId) {
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
