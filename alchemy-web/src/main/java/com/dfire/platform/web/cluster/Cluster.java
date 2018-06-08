package com.dfire.platform.web.cluster;

import org.springframework.beans.factory.DisposableBean;

import com.dfire.platform.web.cluster.request.Request;
import com.dfire.platform.web.cluster.response.Response;

/**
 * @author congbai
 * @date 01/06/2018
 */
public interface Cluster extends DisposableBean {

    String name();

    void start(ClusterInfo clusterInfo);

    Response send(Request message) throws Exception;

}
