package com.dfire.platform.alchemy.web.rest.vm;

import javax.validation.constraints.Pattern;

/**
 * @author congbai
 * @date 2018/6/8
 */
public class JobVM {

    private Long id;

    private Long acServiceId;

    @Pattern(regexp = "^[A-Za-z0-9-]*$")
    private String name;

    private Integer submitMode;

    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

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

    public Integer getSubmitMode() {
        return submitMode;
    }

    public void setSubmitMode(Integer submitMode) {
        this.submitMode = submitMode;
    }

    @Override
    public String toString() {
        return "JobVM{" + "acServiceId=" + acServiceId + ", name='" + name + '\'' + ", submitMode=" + submitMode + '}';
    }
}
