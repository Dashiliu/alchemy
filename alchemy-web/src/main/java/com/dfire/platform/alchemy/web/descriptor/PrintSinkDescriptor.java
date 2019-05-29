package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.print.PrintTableSink;

/**
 * @author congbai
 * @date 2019/5/24
 */
public class PrintSinkDescriptor extends SinkDescriptor{
    @Override
    public String getName() {
        return "print_sink";
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        if (ClusterType.FLINK.equals(clusterType)) {
            return (T) new PrintTableSink();
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);
    }

    @Override
    public String type() {
        return Constants.SINK_TYPE_VALUE_PRINT;
    }

    @Override
    public void validate() throws Exception {
        //nothing to do
    }
}
