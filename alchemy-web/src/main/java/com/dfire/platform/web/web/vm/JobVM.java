package com.dfire.platform.web.web.vm;

/**
 * @author congbai
 * @date 2018/6/8
 */
public class JobVM {

    private Long acServiceId;

    private String name;

    private int submitMode;

    public Long getAcServiceId() {
        return acServiceId;
    }

    public void setAcServiceId(Long acServiceId) {
        this.acServiceId = acServiceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSubmitMode() {
        return submitMode;
    }

    public void setSubmitMode(int submitMode) {
        this.submitMode = submitMode;
    }
}
