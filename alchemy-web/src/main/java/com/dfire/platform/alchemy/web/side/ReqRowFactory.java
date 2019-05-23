package com.dfire.platform.alchemy.web.side;

import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.descriptor.SourceDescriptor;

/**
 * @author congbai
 * @date 2019/5/21
 */
public class ReqRowFactory {

    public static AsyncReqRow getAsync(ClusterType clusterType, SideTableInfo sideTable, SourceDescriptor sideSource)
        throws Exception {
        return sideSource.transform(clusterType, sideSource);
    }

    public static SyncReqRow getSync(ClusterType clusterType, SideTableInfo sideTable, SourceDescriptor sideSource)
        throws Exception {
        return sideSource.transform(clusterType, sideSource);
    }
}
