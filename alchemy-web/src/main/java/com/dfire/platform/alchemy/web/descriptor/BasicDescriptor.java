package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.web.common.ClusterType;

/**
 * @author congbai
 * @date 2018/6/8
 */
public abstract class BasicDescriptor implements Descriptor {

    public static final String NAME = "basic";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        throw new UnsupportedOperationException("UnsupportedOperation");
    }
}
