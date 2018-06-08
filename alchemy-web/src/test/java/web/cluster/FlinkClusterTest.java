package web.cluster;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.flink.runtime.jobmanager.HighAvailabilityMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.junit.Before;
import org.junit.Test;

import com.dfire.platform.web.cluster.ClusterInfo;
import com.dfire.platform.web.cluster.FlinkDefaultCluster;
import com.dfire.platform.web.cluster.request.SqlSubmitRequest;
import com.dfire.platform.web.cluster.response.Response;
import com.dfire.platform.web.common.Field;
import com.dfire.platform.web.common.ReadMode;
import com.dfire.platform.web.common.Table;
import com.dfire.platform.web.common.TimeAttribute;
import com.dfire.platform.web.descriptor.*;

/**
 * @author congbai
 * @date 06/06/2018
 */
public class FlinkClusterTest {

    private FlinkDefaultCluster cluster;

    @Before
    public void before() {
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setName("test");
        clusterInfo.setClusterId("daily");
        clusterInfo.setAddress("10.1.21.95");
        clusterInfo.setMode(HighAvailabilityMode.ZOOKEEPER.toString().toLowerCase());
        clusterInfo.setPort(6123);
        clusterInfo.setStoragePath("hdfs://sunset004.daily.2dfire.info:8020/flink/ha");
        clusterInfo.setZookeeperQuorum("10.1.22.21:2181,10.1.22.22:2181,10.1.22.23:2181");
        this.cluster = new FlinkDefaultCluster();
        cluster.start(clusterInfo);

    }

    @Test
    public void sendJar() {

    }

    @Test
    public void sendScalarSql() throws Exception {
        SqlSubmitRequest sqlSubmitRequest = createSqlRequest(createScalarInputs(), createScalarUdfs(),
            createEsOutputs(), "select scalarF(id) as id,CURRENT_DATE as createTime from kafka_table_test",
            "flinkClusterTest-ScalarSQL");
        Response resp = this.cluster.send(sqlSubmitRequest);
        assert resp.isSuccess();
    }

    @Test
    public void sendTableSql() throws Exception {
        SqlSubmitRequest sqlSubmitRequest
            = createSqlRequest(createTableInputs(), createTableUdfs(), createHbaseOutputs(),
                "SELECT s as body,id FROM kafka_table_test, LATERAL TABLE(tableF(id,999999988889)) as T(s)",
                "flinkClusterTest-TableSQL");
        Response resp = this.cluster.send(sqlSubmitRequest);
        assert resp.isSuccess();
    }

    @Test
    public void sendAggreSql() throws Exception {
        SqlSubmitRequest sqlSubmitRequest = createSqlRequest(createAggreInputs(), createAggreUdfs(),
            createKafkaOutputs(),
            "select aggreF(id) as id from kafka_table_test GROUP BY HOP(ptime, INTERVAL '10' SECOND, INTERVAL '1' SECOND)",
            "flinkClusterTest-AggreSQL");
        Response resp = this.cluster.send(sqlSubmitRequest);
        assert resp.isSuccess();
    }

    private SqlSubmitRequest createSqlRequest(List<SourceDescriptor> inputs, List<UdfDescriptor> udfs,
        List<SinkDescriptor> outputs, String sql, String jobName) {
        SqlSubmitRequest sqlSubmitRequest = new SqlSubmitRequest();
        sqlSubmitRequest.setCheckpointingInterval(10000L);
        sqlSubmitRequest.setJobName(jobName);
        sqlSubmitRequest.setJarPath(
            "/Users/dongbinglin/Code/platform/stream/stream-connectors/target/stream-connectors-1.0-SNAPSHOT.jar");
        sqlSubmitRequest.setClusterName("test");
        sqlSubmitRequest.setInputs(inputs);
        sqlSubmitRequest.setUserDefineFunctions(udfs);
        sqlSubmitRequest.setOutputs(outputs);
        sqlSubmitRequest.setParallelism(1);
        sqlSubmitRequest.setSql(sql);
        sqlSubmitRequest.setTest(true);
        return sqlSubmitRequest;
    }

    private List<SourceDescriptor> createScalarInputs() {
        List<SourceDescriptor> inputs = new ArrayList<>(1);
        KafkaSourceDescriptor kafkaSourceDescriptor = new KafkaSourceDescriptor();
        kafkaSourceDescriptor.setName("kafka_table_test");
        kafkaSourceDescriptor.setBrokers("kafka1001.2dfire-daily.com:9092,kafka1002.2dfire-daily.com:9092");
        kafkaSourceDescriptor.setConsumerGroup("flink_test");
        kafkaSourceDescriptor.setTopic("stream_source");
        Table input = new Table();
        List<Field> inputFields = new ArrayList<>();
        inputFields.add(new Field("id", String.class.getName()));
        inputFields.add(new Field("createTime", Timestamp.class.getName()));
        input.setFields(inputFields);
        Table out = new Table();
        List<Field> outFields = new ArrayList<>();
        outFields.add(new Field("id", String.class.getName()));
        outFields.add(new Field("createTime", Timestamp.class.getName()));
        out.setFields(outFields);
        kafkaSourceDescriptor.setInput(input);
        kafkaSourceDescriptor.setOutput(out);
        inputs.add(kafkaSourceDescriptor);
        return inputs;
    }

    private List<SourceDescriptor> createTableInputs() {
        List<SourceDescriptor> inputs = new ArrayList<>(1);
        KafkaSourceDescriptor kafkaSourceDescriptor = new KafkaSourceDescriptor();
        kafkaSourceDescriptor.setName("kafka_table_test");
        kafkaSourceDescriptor.setBrokers("kafka1001.2dfire-daily.com:9092,kafka1002.2dfire-daily.com:9092");
        kafkaSourceDescriptor.setConsumerGroup("flink_test");
        kafkaSourceDescriptor.setTopic("stream_source");
        Table input = new Table();
        List<Field> inputFields = new ArrayList<>();
        inputFields.add(new Field("id", String.class.getName()));
        inputFields.add(new Field("body", String.class.getName()));
        input.setFields(inputFields);
        Table out = new Table();
        List<Field> outFields = new ArrayList<>();
        outFields.add(new Field("id", String.class.getName()));
        outFields.add(new Field("body", String.class.getName()));
        out.setFields(outFields);
        kafkaSourceDescriptor.setInput(input);
        kafkaSourceDescriptor.setOutput(out);
        inputs.add(kafkaSourceDescriptor);
        return inputs;
    }

    private List<SourceDescriptor> createAggreInputs() {
        List<SourceDescriptor> inputs = new ArrayList<>(1);
        KafkaSourceDescriptor kafkaSourceDescriptor = new KafkaSourceDescriptor();
        kafkaSourceDescriptor.setName("kafka_table_test");
        kafkaSourceDescriptor.setBrokers("kafka1001.2dfire-daily.com:9092,kafka1002.2dfire-daily.com:9092");
        kafkaSourceDescriptor.setConsumerGroup("flink_test");
        kafkaSourceDescriptor.setTopic("stream_source");
        TimeAttribute timeAttribute = new TimeAttribute();
        timeAttribute.setAttribute("ptime");
        timeAttribute.setTimeCharacteristic(TimeCharacteristic.ProcessingTime.toString().toLowerCase());
        kafkaSourceDescriptor.setTimeAttribute(timeAttribute);
        Table out = new Table();
        List<Field> outputFields = new ArrayList<>();
        outputFields.add(new Field("id", String.class.getName()));
        outputFields.add(new Field("ptime", Timestamp.class.getName()));
        out.setFields(outputFields);
        kafkaSourceDescriptor.setOutput(out);
        inputs.add(kafkaSourceDescriptor);
        return inputs;
    }

    private List<UdfDescriptor> createScalarUdfs() {
        List<UdfDescriptor> udfs = new ArrayList<>(1);
        UdfDescriptor udfDescriptor = new UdfDescriptor();
        udfDescriptor.setName("scalarF");
        udfDescriptor.setValue("import com.dfire.platform.api.function.StreamScalarFunction;\n" + "\n" + "/**\n"
            + " * @author congbai\n" + " * @date 06/06/2018\n" + " */\n"
            + "public class TestFunction implements StreamScalarFunction<String> {\n" + "\n" + "    @Override\n"
            + "    public  String invoke(Object... args) {\n" + "        String result=2222;\n"
            + "        return  String.valueOf(result);\n" + "    }\n" + "}\n");
        udfDescriptor.setReadMode(ReadMode.CODE);
        udfs.add(udfDescriptor);
        return udfs;
    }

    private List<UdfDescriptor> createTableUdfs() {
        List<UdfDescriptor> udfs = new ArrayList<>(1);
        UdfDescriptor udfDescriptor = new UdfDescriptor();
        udfDescriptor.setName("tableF");
        udfDescriptor.setValue("import com.dfire.platform.api.function.StreamTableFunction;\n" + "\n" + "/**\n"
            + " * @author congbai\n" + " * @date 06/06/2018\n" + " */\n"
            + "public class TestTableFunction extends StreamTableFunction<String> {\n" + "\n" + "\n" + "    @Override\n"
            + "    public void invoke(Object... args) {\n" + "        for(Object arg:args){\n"
            + "            collect(String.valueOf(arg));\n" + "        }\n" + "    }\n" + "}\n");
        udfDescriptor.setReadMode(ReadMode.CODE);
        udfs.add(udfDescriptor);
        return udfs;
    }

    private List<UdfDescriptor> createAggreUdfs() {
        List<UdfDescriptor> udfs = new ArrayList<>(1);
        UdfDescriptor udfDescriptor = new UdfDescriptor();
        udfDescriptor.setName("aggreF");
        udfDescriptor.setValue("import java.util.ArrayList;\n" + "import java.util.List;\n" + "\n"
            + "import com.dfire.platform.api.function.StreamAggregateFunction;\n" + "\n" + "/**\n"
            + " * @author congbai\n" + " * @date 06/06/2018\n" + " */\n"
            + "public class TestAggreFunction implements StreamAggregateFunction<String, List, Integer> {\n" + "\n"
            + "    @Override\n" + "    public List createAccumulator() {\n" + "        return new ArrayList();\n"
            + "    }\n" + "\n" + "    @Override\n" + "    public void accumulate(List accumulator, String value) {\n"
            + "        accumulator.add(value);\n" + "    }\n" + "\n" + "    @Override\n"
            + "    public Integer getValue(List accumulator) {\n" + "        return accumulator.size();\n" + "    }\n"
            + "}\n");
        udfDescriptor.setReadMode(ReadMode.CODE);
        udfs.add(udfDescriptor);
        return udfs;
    }

    private List<SinkDescriptor> createEsOutputs() {
        List<SinkDescriptor> outputs = new ArrayList<>(1);
        EsSinkDescriptor sinkDescriptor = new EsSinkDescriptor();
        sinkDescriptor.setName("esSink");
        sinkDescriptor.setClusterName("daily");
        sinkDescriptor.setAddress("10.1.21.61:9300,10.1.21.62:9300,10.1.21.63:9300");
        sinkDescriptor.setIndex("flink-test");
        sinkDescriptor.setBufferSize(2);
        outputs.add(sinkDescriptor);
        return outputs;
    }

    private List<SinkDescriptor> createKafkaOutputs() {
        List<SinkDescriptor> outputs = new ArrayList<>(1);
        KafkaSinkDescriptor sinkDescriptor = new KafkaSinkDescriptor();
        sinkDescriptor.setName("kafka_sink");
        sinkDescriptor.setTopic("stream_dest");
        sinkDescriptor.setBrokers("kafka1001.2dfire-daily.com:9092,kafka1002.2dfire-daily.com:9092");
        outputs.add(sinkDescriptor);
        return outputs;
    }

    private List<SinkDescriptor> createHbaseOutputs() {
        List<SinkDescriptor> outputs = new ArrayList<>(1);
        HbaseSinkDescriptor sinkDescriptor = new HbaseSinkDescriptor();
        sinkDescriptor.setName("hbase_sink");
        sinkDescriptor.setBufferSize(1048576L);
        sinkDescriptor.setFamily("s");
        sinkDescriptor.setNode("/hbase-unsecure");
        sinkDescriptor.setTableName("flink-test");
        sinkDescriptor.setZookeeper("10.1.22.21:2181,10.1.22.22:2181,10.1.22.23:2181");
        sinkDescriptor.setReadMode(ReadMode.JAR);
        sinkDescriptor.setValue("web.cluster.TestHbaseInvoke");
        // sinkDescriptor.setValue("import com.dfire.platform.api.sink.HbaseInvoker;\n" +
        // "\n" +
        // "/**\n" +
        // " * @author congbai\n" +
        // " * @date 07/06/2018\n" +
        // " */\n" +
        // "public class TestHbaseInvoke implements HbaseInvoker{\n" +
        // " @Override\n" +
        // " public String getRowKey(Object[] rows) {\n" +
        // " return String.valueOf(rows[0]);\n" +
        // " }\n" +
        // "\n" +
        // " @Override\n" +
        // " public String getQualifier(Object[] rows) {\n" +
        // " return String.valueOf(rows[0]);\n" +
        // " }\n" +
        // "}\n");
        outputs.add(sinkDescriptor);
        return outputs;
    }

}
