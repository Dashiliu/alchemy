package com.dfire.platform.alchemy.web.cluster.response;

/**
 * @author congbai
 * @date 2018/6/20
 */
public class JobStatusResponse extends StatusJobResponse {

    private Integer status;

    public JobStatusResponse(boolean success, Integer status) {
        super(success);
        this.status = status;
    }

    public JobStatusResponse(String message) {
        super(message);
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
