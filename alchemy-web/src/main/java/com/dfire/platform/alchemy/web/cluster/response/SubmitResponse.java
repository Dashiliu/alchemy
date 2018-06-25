package com.dfire.platform.alchemy.web.cluster.response;

/**
 * @author congbai
 * @date 2018/6/20
 */
public class SubmitResponse extends Response {

    public SubmitResponse(boolean success) {
        super(success);
    }

    public SubmitResponse(String message) {
        super(message);
    }
}
