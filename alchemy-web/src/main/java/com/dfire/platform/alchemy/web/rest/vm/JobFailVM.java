package com.dfire.platform.alchemy.web.rest.vm;

import javax.validation.constraints.NotNull;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class JobFailVM {

    @NotNull
    private Long jobId;

    @NotNull
    private String msg;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
