package com.dfire.platform.alchemy.web.descriptor;

/**
 * @author congbai
 * @date 01/06/2018
 */
public interface Descriptor {

    String getType();

    void validate() throws Exception;

}
