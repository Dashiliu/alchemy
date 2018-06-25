package com.dfire.platform.alchemy.web.cluster.response;

/**
 * @author congbai
 * @date 2018/6/20
 */
public class ListJobResponse extends Response {

    public ListJobResponse(boolean success) {
        super(success);
    }

    public ListJobResponse(String message) {
        super(message);
    }
}
