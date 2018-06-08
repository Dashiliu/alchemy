package com.dfire.platform.web.descriptor;

import com.dfire.platform.web.common.ClusterType;

/**
 * @author congbai
 * @date 03/06/2018
 */
public class TidbSinkDescriptor extends SinkDescriptor {

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        return null;
    }

    @Override
    public void validate() throws Exception {

    }
}
