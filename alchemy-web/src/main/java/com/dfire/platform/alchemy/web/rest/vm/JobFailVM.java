package com.dfire.platform.alchemy.web.rest.vm;

import javax.validation.constraints.NotNull;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class JobFailVM {

    @NotNull
    private Long acJobId;

    @NotNull
    private String msg;

    public Long getAcJobId() {
        return acJobId;
    }

    public void setAcJobId(Long acJobId) {
        this.acJobId = acJobId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
