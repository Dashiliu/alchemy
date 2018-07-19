package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.web.common.Constants;

/**
 * @author congbai
 * @date 2018/6/30
 */
public class FormatDescriptor implements Descriptor {

    private Integer propertyVersion;

    private String schema;

    private String type;

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

    @Override
    public String getType() {
        return Constants.TYPE_VALUE_FORMAT;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void validate() throws Exception {

    }
}
