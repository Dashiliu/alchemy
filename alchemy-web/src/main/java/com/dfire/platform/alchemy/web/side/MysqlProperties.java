package com.dfire.platform.alchemy.web.side;

import java.io.Serializable;

/**
 * @author congbai
 * @date 2019/5/23
 */
public class MysqlProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private String url;

    private String userName;

    private String password;

    private Integer maxPoolSize;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
}
