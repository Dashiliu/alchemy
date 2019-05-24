package web;

import com.dfire.platform.alchemy.web.cluster.ClusterInfo;
import com.dfire.platform.alchemy.web.cluster.flink.FlinkCluster;
import org.apache.flink.runtime.jobmanager.HighAvailabilityMode;
import org.junit.Before;

/**
 * @author congbai
 * @date 2019/5/24
 */
public class BaseCluster {

    protected FlinkCluster cluster;

    @Before
    public void before() {
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setName("test");
        clusterInfo.setClusterId("daily-default-8");
        clusterInfo.setAddress("10.1.21.95");
        clusterInfo.setMode(HighAvailabilityMode.ZOOKEEPER.toString().toLowerCase());
        clusterInfo.setPort(6123);
        clusterInfo.setStoragePath("hdfs://sunset002.daily.2dfire.info:8020/flink/ha/real");
        clusterInfo.setZookeeperQuorum("10.1.22.21:2181,10.1.22.22:2181,10.1.22.23:2181");
        this.cluster = new FlinkCluster();
        cluster.start(clusterInfo);
    }

}
