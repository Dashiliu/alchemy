package com.dfire.platform.alchemy.web.rest.vm;

import javax.validation.constraints.Pattern;

/**
 * @author congbai
 * @date 2018/6/8
 */
public class JobVM {

    private Long acServiceId;

    @Pattern(regexp = "[a-zA-Z]")
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

    @Override
    public String toString() {
        return "JobVM{" + "acServiceId=" + acServiceId + ", name='" + name + '\'' + ", submitMode=" + submitMode + '}';
    }
}
