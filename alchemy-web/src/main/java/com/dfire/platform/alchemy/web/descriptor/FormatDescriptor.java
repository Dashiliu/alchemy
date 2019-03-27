package com.dfire.platform.alchemy.web.descriptor;

/**
 * @author congbai
 * @date 2018/6/30
 */
public class FormatDescriptor implements Descriptor {

    private Integer propertyVersion;

    private String schema;

    private String className;

    private String type;

    private String regular;

    public Integer getPropertyVersion() {
        return propertyVersion;
    }

    public void setPropertyVersion(Integer propertyVersion) {
        this.propertyVersion = propertyVersion;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void validate() throws Exception {

    }

    public String getRegular() {
        return regular;
    }

    public void setRegular(String regular) {
        this.regular = regular;
    }
}
