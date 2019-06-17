package com.dfire.platform.alchemy.job;

import com.dfire.platform.alchemy.client.ClusterClientFactory;
import com.dfire.platform.alchemy.client.FlinkClient;
import com.dfire.platform.alchemy.client.StandaloneClusterInfo;
import com.dfire.platform.alchemy.util.BindPropertiesUtil;
import org.junit.Before;
import org.springframework.util.ResourceUtils;

import java.io.File;

public class BaseCluster {

    FlinkClient client;

    @Before
    public void before() throws Exception {
        File file = ResourceUtils.getFile("classpath:yaml/cluster.yaml");
        StandaloneClusterInfo clusterInfo = BindPropertiesUtil.bindProperties(file, StandaloneClusterInfo.class);
        client = ClusterClientFactory.createRestClient(clusterInfo);
    }



}
