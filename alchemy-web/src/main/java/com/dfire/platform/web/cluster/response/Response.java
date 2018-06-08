package com.dfire.platform.web.cluster.response;

/**
 * @author congbai
 * @date 01/06/2018
 */
public class Response {

    private boolean success;

    private String message;

    public Response(boolean success) {
        this.success = success;
    }

    public Response(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
