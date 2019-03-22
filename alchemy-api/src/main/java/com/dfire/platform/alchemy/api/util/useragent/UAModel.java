package com.dfire.platform.alchemy.api.util.useragent;

import java.io.Serializable;

public class UAModel implements Serializable {

    private String ua_device;

    private String ua_major;

    private String ua_minor;

    private String ua_name; //Andriod

    private String ua_os;

    private String ua_os_major;

    private String ua_os_minor;

    private String ua_os_name;

    private String ua_patch;

    private String ua_build;

    public String getUa_build() {
        return ua_build;
    }

    public void setUa_build(String ua_build) {
        this.ua_build = ua_build;
    }

    public String getUa_device() {
        return ua_device;
    }

    public void setUa_device(String ua_device) {
        this.ua_device = ua_device;
    }

    public String getUa_major() {
        return ua_major;
    }

    public void setUa_major(String ua_major) {
        this.ua_major = ua_major;
    }

    public String getUa_minor() {
        return ua_minor;
    }

    public void setUa_minor(String ua_minor) {
        this.ua_minor = ua_minor;
    }

    public String getUa_name() {
        return ua_name;
    }

    public void setUa_name(String ua_name) {
        this.ua_name = ua_name;
    }

    public String getUa_os() {
        return ua_os;
    }

    public void setUa_os(String ua_os) {
        this.ua_os = ua_os;
    }

    public String getUa_os_major() {
        return ua_os_major;
    }

    public void setUa_os_major(String ua_os_major) {
        this.ua_os_major = ua_os_major;
    }

    public String getUa_os_minor() {
        return ua_os_minor;
    }

    public void setUa_os_minor(String ua_os_minor) {
        this.ua_os_minor = ua_os_minor;
    }

    public String getUa_os_name() {
        return ua_os_name;
    }

    public void setUa_os_name(String ua_os_name) {
        this.ua_os_name = ua_os_name;
    }

    public String getUa_patch() {
        return ua_patch;
    }

    public void setUa_patch(String ua_patch) {
        this.ua_patch = ua_patch;
    }
}
