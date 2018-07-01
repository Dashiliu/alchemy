package com.dfire.platform.alchemy.web.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author congbai
 * @date 2018/6/29
 */
public class Content {

    private String config="";

    private List<String> code=new ArrayList<>();

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public List<String> getCode() {
        return code;
    }

    public void setCode(List<String> code) {
        this.code = code;
    }
}
