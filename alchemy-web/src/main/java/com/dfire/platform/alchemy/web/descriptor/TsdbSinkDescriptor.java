package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;

/**
 * @author congbai
 * @date 03/06/2018
 */
public class TsdbSinkDescriptor extends SinkDescriptor {

    private String name;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        return null;
    }

    @Override
    public void validate() throws Exception {

    }

    @Override
    public String getType() {
        return Constants.SINK_TYPE_VALUE_OPENTSDB;
    }
}
