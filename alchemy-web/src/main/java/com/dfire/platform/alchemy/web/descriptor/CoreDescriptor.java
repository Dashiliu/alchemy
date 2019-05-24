package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.web.common.ClusterType;

/**
 * @author congbai
 * @date 01/06/2018
 */
public interface CoreDescriptor extends Descriptor {

    String getName();

    <T> T transform(ClusterType clusterType) throws Exception;

    default <T,R> T transform(ClusterType clusterType, R param) throws Exception{
        return transform(clusterType);
    }

}
