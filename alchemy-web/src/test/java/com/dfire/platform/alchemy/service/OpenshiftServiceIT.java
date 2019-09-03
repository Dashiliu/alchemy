package com.dfire.platform.alchemy.service;

import com.dfire.platform.alchemy.client.OpenshiftClusterInfo;
import com.dfire.platform.alchemy.config.OpenshiftProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OpenshiftServiceIT {

    OpenshiftService openshiftService;


    @Before
    public void before() throws IOException, InterruptedException {
        OpenshiftProperties openshiftProperties = new OpenshiftProperties();
        openshiftProperties.setUsername("congbai");
        openshiftProperties.setPassword("jiayou1114");
        openshiftProperties.setNamespace("flink");
        openshiftProperties.setHadoopUserName("flink");
        openshiftProperties.setHadoopVolumeName("hadoop");
        openshiftProperties.setServiceAccount("flink");
        openshiftProperties.setServiceAccountName("flink");
        openshiftProperties.setUrl("https://cs.2dfire.tech");
        openshiftService = new OpenshiftService(null, new RestTemplate(), openshiftProperties);
        long startTimestamp = System.currentTimeMillis();
        while (openshiftService.getToken() == null){
            Thread.sleep(10);
            if(System.currentTimeMillis() - startTimestamp > 10000){
                throw new RuntimeException("Failed to get Token");
            }
        }
    }


    @Test
    public void create() {
        OpenshiftClusterInfo openshiftClusterInfo = createInfo();
        openshiftService.create(openshiftClusterInfo);
    }

    @Test
    public void update() {
        OpenshiftClusterInfo openshiftClusterInfo = createInfo();
        openshiftClusterInfo.setReplicas(5);
        openshiftClusterInfo.setJobManagerResources(new OpenshiftClusterInfo.Resources(new OpenshiftClusterInfo.Resource("2", "4G"), new OpenshiftClusterInfo.Resource("3", "8G")));
        openshiftClusterInfo.setTaskManagerResources(new OpenshiftClusterInfo.Resources(new OpenshiftClusterInfo.Resource("500m", "2G"), new OpenshiftClusterInfo.Resource("1", "3G")));
        openshiftService.update(openshiftClusterInfo);
    }

    @Test
    public void delete() {
        OpenshiftClusterInfo openshiftClusterInfo = createInfo();
        openshiftService.delete(openshiftClusterInfo);
    }


    private OpenshiftClusterInfo createInfo() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("high-availability.jobmanager.port", "6123");
        configs.put("high-availability", "zookeeper");
        configs.put("high-availability.storageDir", "/flink/ha/default");
        configs.put("high-availability.cluster-id", "test");
        configs.put("high-availability.zookeeper.quorum", "10.1.22.20,10.1.22.26,10.1.22.24");
        configs.put("env.java.opts.taskmanager", "-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -Xmn6G -XX:MaxDirectMemorySize=4096m  -XX:MetaspaceSize=1024m  -XX:MaxMetaspaceSize=1024m -Xss256k -XX:G1HeapRegionSize=16m -XX:G1ReservePercent=30 -XX:InitiatingHeapOccupancyPercent=30 -XX:+PrintGCDetails   -XX:+PrintGCDateStamps  -XX:+PrintHeapAtGC -XX:+PrintPromotionFailure  -Xloggc:/opt/logs/flink/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/logs/flink/java.hprof -XX:ErrorFile=/opt/logs/flink/hs_err_pid%p.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=20M\n" +
            "jobmanager.heap.mb: 4024");
        configs.put("env.java.opts.jobmanager","-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -Xmn4G  -XX:MetaspaceSize=1024m  -XX:MaxMetaspaceSize=1024m -Xss228k -XX:G1HeapRegionSize=16m -XX:G1ReservePercent=30 -XX:InitiatingHeapOccupancyPercent=30 -XX:+PrintGCDetails   -XX:+PrintGCDateStamps  -XX:+PrintHeapAtGC -XX:+PrintPromotionFailure");
        OpenshiftClusterInfo openshiftClusterInfo = new OpenshiftClusterInfo();
        openshiftClusterInfo.setImage("quay.app.2dfire.com/congbai/flink:1.8.0-alchemy");
        openshiftClusterInfo.setName("client-cluster");
        openshiftClusterInfo.setJobManagerAddress("jobmanager-client-cluster");
        openshiftClusterInfo.setReplicas(2);
        openshiftClusterInfo.setConfigs(configs);
        openshiftClusterInfo.setJobManagerResources(new OpenshiftClusterInfo.Resources(new OpenshiftClusterInfo.Resource("1", "3G"), new OpenshiftClusterInfo.Resource("3", "8G")));
        openshiftClusterInfo.setTaskManagerResources(new OpenshiftClusterInfo.Resources(new OpenshiftClusterInfo.Resource("300m", "2G"), new OpenshiftClusterInfo.Resource("1", "3G")));
        return openshiftClusterInfo;
    }

}
