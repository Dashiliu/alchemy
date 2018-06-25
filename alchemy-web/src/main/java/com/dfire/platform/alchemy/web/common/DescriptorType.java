package com.dfire.platform.alchemy.web.common;

/**
 * @author congbai
 * @date 05/06/2018
 */
public enum DescriptorType {

    SOURCE(1),

    UDF(1),

    SINK(2);

    private int type;

    DescriptorType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
