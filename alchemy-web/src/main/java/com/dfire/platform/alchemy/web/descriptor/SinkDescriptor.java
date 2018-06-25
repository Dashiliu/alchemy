package com.dfire.platform.alchemy.web.descriptor;

/**
 * @author congbai
 * @date 03/06/2018
 */
public abstract class SinkDescriptor implements Descriptor {

    private String name;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
