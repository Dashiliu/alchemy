package com.dfire.platform.alchemy.web.descriptor;

/**
 * @author congbai
 * @date 01/06/2018
 */
public interface Descriptor {

    String type();

    void validate() throws Exception;

}
