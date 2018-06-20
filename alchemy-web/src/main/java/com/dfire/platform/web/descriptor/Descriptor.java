package com.dfire.platform.web.descriptor;

import com.dfire.platform.web.common.ClusterType;

/**
 * @author congbai
 * @date 01/06/2018
 */
public interface Descriptor {

    String getName();

    String getContentType();

    <T> T transform(ClusterType clusterType) throws Exception;

    void validate() throws Exception;

}
