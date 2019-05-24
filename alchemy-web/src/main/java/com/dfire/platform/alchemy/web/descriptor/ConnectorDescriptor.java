package com.dfire.platform.alchemy.web.descriptor;

import java.util.List;

import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Field;

/**
 * @author congbai
 * @date 2018/6/30
 */
public interface ConnectorDescriptor extends Descriptor {

    <T> T buildSource(ClusterType clusterType, List<Field> schema, FormatDescriptor format) throws Exception;

    default <T,R> T buildSource(ClusterType clusterType, List<Field> schema, FormatDescriptor format, R param) throws Exception{
        return buildSource(clusterType, schema, format);
    }
}
