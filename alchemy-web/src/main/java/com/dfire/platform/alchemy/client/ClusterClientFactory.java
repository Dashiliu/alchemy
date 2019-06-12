package com.dfire.platform.alchemy.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.StandaloneClusterClient;
import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.AkkaOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.HighAvailabilityOptions;
import org.apache.flink.configuration.JobManagerOptions;

import com.dfire.platform.alchemy.domain.Cluster;
import com.dfire.platform.alchemy.domain.enumeration.ClusterType;
import com.dfire.platform.alchemy.util.BindPropertiesUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author congbai
 * @date 2019/5/15
 */
public class ClusterClientFactory {

    public static FlinkClient get(Cluster cluster) throws Exception {
        ClusterType clusterType = cluster.getType();
        switch (clusterType) {
            case STANDALONE:
                return createRestClient(cluster);
            case YARN:
                // todo 支持yarn client
            default:
                throw new UnsupportedOperationException("only support rest client ");
        }
    }

    private static FlinkClient createRestClient(Cluster cluster) throws Exception {
        StandaloneClusterInfo clusterInfo = BindPropertiesUtil.bindProperties(cluster.getConfig(), StandaloneClusterInfo.class);
        Configuration configuration = new Configuration();
////        configuration.setString(HighAvailabilityOptions.HA_MODE, clusterInfo.getMode());
//        if (StringUtils.isNotEmpty(clusterInfo.getClusterId())) {
//            configuration.setString(HighAvailabilityOptions.HA_CLUSTER_ID, clusterInfo.getClusterId());
//        }
//        if (StringUtils.isNotEmpty(clusterInfo.getZookeeperQuorum())) {
//            configuration.setString(HighAvailabilityOptions.HA_ZOOKEEPER_QUORUM, clusterInfo.getZookeeperQuorum());
//        }
//        if (StringUtils.isNotEmpty(clusterInfo.getStoragePath())) {
//            configuration.setString(HighAvailabilityOptions.HA_STORAGE_PATH, clusterInfo.getStoragePath());
//        }
//        if (StringUtils.isNotEmpty(clusterInfo.getAddress())) {
//            configuration.setString(JobManagerOptions.ADDRESS, clusterInfo.getAddress());
//        }
//        if (StringUtils.isNotEmpty(clusterInfo.getLookupTimeout())) {
//            configuration.setString(AkkaOptions.LOOKUP_TIMEOUT, clusterInfo.getLookupTimeout());
//        }
//        if (clusterInfo.getPort() != null) {
//            configuration.setInteger(JobManagerOptions.PORT, clusterInfo.getPort());
//        }
        if(clusterInfo.getConfiguration() != null){
            for(Map.Entry<String, Object> entry : clusterInfo.getConfiguration().entrySet()){
                configuration.setString(entry.getKey(), entry.getValue().toString());
            }
        }
        try {
            StandaloneClusterClient clusterClient = new StandaloneClusterClient(configuration);
            clusterClient.setPrintStatusDuringExecution(true);
            clusterClient.setDetached(true);
            return new StandaloneClusterFlinkClient(clusterClient, clusterInfo.getAvgs());
        } catch (Exception e) {
            throw new RuntimeException("Cannot establish connection to JobManager: " + e.getMessage(), e);
        }
    }

}
