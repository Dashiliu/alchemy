package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.web.cluster.flink.SqlSubmitFlinkRequest;
import com.dfire.platform.alchemy.web.util.BindPropertiesUtils;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.flink.table.client.config.ConfigUtil;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;


/**
 * @author congbai
 * @date 2018/6/30
 */
public class BindPropertiesToTargetTest {

    @Test
    public void bindFile() throws Exception {
        File file = ResourceUtils.getFile("classpath:config.yaml");
        ObjectMapper objectMapper = new ConfigUtil.LowerCaseYamlMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SqlSubmitFlinkRequest sqlSubmitFlinkRequest = objectMapper.readValue(file.toURL(), SqlSubmitFlinkRequest.class);
        ConnectorDescriptor connectorDescriptor=sqlSubmitFlinkRequest.getTable().getSources().get(0).getConnectorDescriptor();
        assert  connectorDescriptor != null ;
    }

    @Test
    public void bindString() throws Exception {
        String yaml = yaml();
        SqlSubmitFlinkRequest sqlSubmitFlinkRequest = BindPropertiesUtils.bindProperties(yaml, SqlSubmitFlinkRequest.class);
        ConnectorDescriptor connectorDescriptor=sqlSubmitFlinkRequest.getTable().getSources().get(0).getConnectorDescriptor();
        assert  connectorDescriptor != null ;
    }

    private String yaml() {
        return "    name: test\n" +
            "    cluster:\n" +
            "    parallelism: 1\n" +
            "    time-characteristic: EventTime\n" +
            "    checkpointing-interval: 60000 #ms\n" +
            "    table:\n" +
            "        #        sql: select * from  TaxiRides\n" +
            "        sources:\n" +
            "        - name: kafka_table_test\n" +
            "          schema:\n" +
            "          - name: id\n" +
            "            type: VARCHAR\n" +
            "          - name: rideId\n" +
            "            type: BIGINT\n" +
            "          - name: lon\n" +
            "            type: FLOAT\n" +
            "          - name: lat\n" +
            "            type: FLOAT\n" +
            "          - name: procTime\n" +
            "            type: TIMESTAMP\n" +
            "            proctime: true\n" +
            "          - name: rideTime\n" +
            "            type: TIMESTAMP\n" +
            "            rowtime:\n" +
            "                timestamps:\n" +
            "                    type: \"from-field\"\n" +
            "                    from: \"rideTime\"\n" +
            "                watermarks:\n" +
            "                    type: \"periodic-bounded\"\n" +
            "                    delay: \"1000\"\n" +
            "          connector:\n" +
            "              property-version: 1\n" +
            "              type: kafka\n" +
            "              version: 0.11\n" +
            "              topic: TaxiRides\n" +
            "              startup-mode: earliest-offset\n" +
            "              properties:\n" +
            "               zookeeper.connect: localhost:2181\n" +
            "               bootstrap.servers:  localhost:9092\n" +
            "               group.id: testGroup\n" +
            "          format:\n" +
            "              property-version: 1\n" +
            "              type: json\n" +
            "              schema: \"ROW(rideId LONG, lon FLOAT, lat FLOAT, rideTime TIMESTAMP)\"\n" +
            "        - name: rocketmq_table_test\n" +
            "          schema:\n" +
            "          - name: goodId\n" +
            "            type: VARCHAR\n" +
            "          - name: entityId\n" +
            "            type: VARCHAR\n" +
            "          - name: rideTime\n" +
            "            type: TIMESTAMP\n" +
            "            rowtime:\n" +
            "                timestamps:\n" +
            "                    type: \"from-field\"\n" +
            "                    from: \"rideTime\"\n" +
            "                watermarks:\n" +
            "                    type: \"periodic-bounded\"\n" +
            "                    delay: \"1000\"\n" +
            "          connector:\n" +
            "              type: rocketMQ\n" +
            "              name-servers: mq101.2dfire-daily.com:9876;mq102.2dfire-daily.com:9876\n" +
            "              name: hot_goods\n" +
            "              topic: hot_goods\n" +
            "#              tag: *\n" +
            "              consumer-group: c_alchemy_hot_goods\n" +
            "          format:\n" +
            "              property-version: 1\n" +
            "              type: json\n" +
            "              schema: \"ROW(entityId VARCHAR,goodId VARCHAR, rideTime TIMESTAMP)\"\n" +
            "        udfs:\n" +
            "        - name: scalarF\n" +
            "          value: ${0}\n" +
            "        - name: tableF\n" +
            "          value: ${1}\n" +
            "        - name: aggreF\n" +
            "          value: ${2}\n" +
            "        sinks:\n" +
            "        - name: esSink\n" +
            "          type: elasticsearch\n" +
            "          cluster-name: daily\n" +
            "          address: 10.1.21.61:9300,10.1.21.62:9300,10.1.21.63:9300\n" +
            "          index: flink-test\n" +
            "          buffer-size: 2  #M\n" +
            "        - name: hbaseSink\n" +
            "          type: hbase\n" +
            "          buffer-size: 1048576 # byte\n" +
            "          family: s\n" +
            "          node: /hbase-unsecure\n" +
            "          table-name: flink-test\n" +
            "          zookeeper: 10.1.22.21:2181,10.1.22.22:2181,10.1.22.23:2181\n" +
            "          value: ${3}\n" +
            "        - name: kafkaSink\n" +
            "          type: kafka\n" +
            "          topic: stream_dest\n" +
            "          properties:\n" +
            "          - key: bootstrap.servers\n" +
            "            value: kafka1001.2dfire-daily.com:9092,kafka1002.2dfire-daily.com:9092\n" +
            "        - name: hot_notify\n" +
            "          type: rocketMQ\n" +
            "          topic: alchemy\n" +
            "          tag: hot_goods\n" +
            "          name-servers: mq101.2dfire-daily.com:9876;mq102.2dfire-daily.com:9876\n" +
            "          producer-group: c_alchemy_producer\n";
    }


}
