package com.dfire.platform.alchemy.web.cluster.flink;

import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.StandaloneClusterClient;
import org.apache.flink.configuration.AkkaOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.HighAvailabilityOptions;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.runtime.jobmanager.HighAvailabilityMode;

import com.dfire.platform.alchemy.web.cluster.ClusterInfo;

/**
 * @author congbai
 * @date 2019/5/15
 */
public class ClusterClientFactory {

    public static ClusterClient get(ClusterInfo clusterInfo) {
        Configuration configuration = new Configuration();
        configuration.setString(HighAvailabilityOptions.HA_MODE,
            HighAvailabilityMode.ZOOKEEPER.toString().toLowerCase());
        configuration.setString(HighAvailabilityOptions.HA_CLUSTER_ID, clusterInfo.getClusterId());
        configuration.setString(HighAvailabilityOptions.HA_ZOOKEEPER_QUORUM, clusterInfo.getZookeeperQuorum());
        configuration.setString(HighAvailabilityOptions.HA_STORAGE_PATH, clusterInfo.getStoragePath());
        configuration.setString(JobManagerOptions.ADDRESS, clusterInfo.getAddress());
        configuration.setString(AkkaOptions.LOOKUP_TIMEOUT, "30 s");
        configuration.setInteger(JobManagerOptions.PORT, clusterInfo.getPort());
        try {
            ClusterClient clusterClient = new StandaloneClusterClient(configuration);
            clusterClient.setPrintStatusDuringExecution(true);
            clusterClient.setDetached(true);
            return clusterClient;
        } catch (Exception e) {
            throw new RuntimeException("Cannot establish connection to JobManager: " + e.getMessage(), e);
        }
    }

}
