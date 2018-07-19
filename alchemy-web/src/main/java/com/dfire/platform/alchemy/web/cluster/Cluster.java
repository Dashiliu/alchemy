package com.dfire.platform.alchemy.web.cluster;

import org.springframework.beans.factory.DisposableBean;

import com.dfire.platform.alchemy.web.cluster.request.Request;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.common.ClusterType;

/**
 * @author congbai
 * @date 01/06/2018
 */
public interface Cluster extends DisposableBean {

    ClusterType clusterType();

    void start(ClusterInfo clusterInfo);

    Response send(Request message) throws Exception;

}
