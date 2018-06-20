package com.dfire.platform.web.service.dto;

import java.util.Date;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class JobFailMsgDTO {

    private String msg;

    private Date createTime;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
