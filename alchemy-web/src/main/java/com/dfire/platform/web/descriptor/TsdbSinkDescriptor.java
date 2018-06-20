package com.dfire.platform.web.descriptor;

import org.springframework.stereotype.Component;

import com.dfire.platform.web.common.ClusterType;

/**
 * @author congbai
 * @date 03/06/2018
 */
@Component
public class TsdbSinkDescriptor extends SinkDescriptor {

    @Override
    public String getContentType() {
        return "tsdbSink";
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        return null;
    }

    @Override
    public void validate() throws Exception {

    }
}
