package web.cluster;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.dfire.platform.alchemy.function.aggregate.FlinkAllAggregateFunction;
import org.apache.flink.api.common.JobSubmissionResult;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.client.program.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.HighAvailabilityOptions;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.optimizer.DataStatistics;
import org.apache.flink.optimizer.Optimizer;
import org.apache.flink.optimizer.costs.DefaultCostEstimator;
import org.apache.flink.optimizer.plan.FlinkPlan;
import org.apache.flink.runtime.jobmanager.HighAvailabilityMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.Kafka010JsonTableSink;
import org.apache.flink.streaming.connectors.kafka.Kafka010JsonTableSource;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.Test;

import com.dfire.platform.alchemy.web.util.PropertiesUtils;

/**
 * @author congbai
 * @date 30/05/2018
 */
public class StreamExecutionEnvironmentTest {
    private static final TableSchema TABLE_SCHEMA
        = new TableSchema(new String[] {"id"}, new TypeInformation<?>[] {BasicTypeInfo.STRING_TYPE_INFO});

    private static final TableSchema JSON_SCHEMA
        = new TableSchema(new String[] {"id",}, new TypeInformation<?>[] {BasicTypeInfo.STRING_TYPE_INFO});

    @Test
    public void sql() throws Exception {
        String filePath = "/Users/dongbinglin/Code/benchmark/stream-test/target/stream-test-1.0-SNAPSHOT.jar";
        List<URL> jarFiles = createPath(filePath);
        Map<String, String> prop = new HashMap<>();
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, "stream_test");
        prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
            "kafka1001.2dfire-daily.com:9092,kafka1002.2dfire-daily.com:9092");
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        Properties conf = PropertiesUtils.getProperties(prop);
        Kafka010JsonTableSource kafka010JsonTableSource = Kafka010JsonTableSource.builder().forTopic("stream_source")
            .fromEarliest().withSchema(TABLE_SCHEMA).forJsonSchema(JSON_SCHEMA).withKafkaProperties(conf)
            // .withProctimeAttribute("time")
            .build();
        // set up the execution environment
        // ClusterClient client = createClusterClient();
        final StreamExecutionEnvironment execEnv = StreamExecutionEnvironment.createLocalEnvironment();
        execEnv.setParallelism(2);
        execEnv.enableCheckpointing(1000);
        StreamTableEnvironment env = StreamTableEnvironment.getTableEnvironment(execEnv);
        execEnv.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        env.registerTableSource("test", kafka010JsonTableSource);
        env.registerFunction("testJoin",
            new FlinkAllAggregateFunction("\n" + "import com.dfire.platform.api.StreamAggregateFunction;\n"
                + "import org.apache.commons.lang3.StringUtils;\n" + "\n" + "/**\n" + " * @author congbai\n"
                + " * @date 31/05/2018\n" + " */\n"
                + "public class Aggregate extends StreamAggregateFunction<String,String>{\n" + "\n"
                + "    private String accumulator;\n" + "\n" + "    @Override\n"
                + "    public String createAccumulator() {\n" + "        this.accumulator=\"\";\n"
                + "        return accumulator;\n" + "    }\n" + "\n" + "    @Override\n"
                + "    public void accumulate(String accumulator, String value) {\n"
                + "        this.accumulator= StringUtils.join(accumulator,value);\n" + "    }\n" + "\n"
                + "    @Override\n" + "    public String getValue(String accumulator) {\n"
                + "        return this.accumulator;\n" + "    }\n" + "}\n", "table"));
        // Table table = env.sqlQuery("SELECT s as id FROM test, LATERAL TABLE(testJoin(id,99999999)) as T(s)");
        Table table = env.sqlQuery(
            "SELECT testJoin(id) as id from test GROUP BY HOP(CURRENT_TIME, INTERVAL '10' MINUTE, INTERVAL '1' MINUTE)");
        Kafka010JsonTableSink kafka010JsonTableSink = new Kafka010JsonTableSink("stream_dest", conf);
        table.writeToSink(kafka010JsonTableSink);
        // StreamGraph streamGraph= execEnv.getStreamGraph();
        // streamGraph.setJobName("test");
        // ClassLoader usercodeClassLoader = JobWithJars.buildUserCodeClassLoader(jarFiles, Collections.emptyList(),
        // getClass().getClassLoader());
        // client.run(streamGraph, jarFiles, Collections.emptyList(), usercodeClassLoader);
        execEnv.execute("testsql");

    }

    private List<URL> createPath(String filePath) {
        List<URL> jarFiles = new ArrayList<>(1);

        try {
            URL jarFileUrl = new File(filePath).getAbsoluteFile().toURI().toURL();
            jarFiles.add(jarFileUrl);
            JobWithJars.checkJarFile(jarFileUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("JAR file path is invalid '" + filePath + "'", e);
        } catch (IOException e) {
            throw new RuntimeException("Problem with jar file " + filePath, e);
        }
        return jarFiles;
    }

    @Test
    public void jar() throws ProgramInvocationException {
        String filePath = "/Users/dongbinglin/Code/platform/sunset/target/sunset-0.0.1-SNAPSHOT.jar";
        PackagedProgram program
            = new PackagedProgram(new File(filePath), "com.dfire.platform.sunset.magiceye.Track", new String[] {});
        ClassLoader classLoader = program.getUserCodeClassLoader();

        Optimizer optimizer = new Optimizer(new DataStatistics(), new DefaultCostEstimator(), new Configuration());
        FlinkPlan plan = ClusterClient.getOptimizedPlan(optimizer, program, 2);
        // set up the execution environment
        ClusterClient client = createClusterClient();
        List<URL> jarFiles = createPath(filePath);
        try {
            JobSubmissionResult submissionResult = client.run(plan, jarFiles, Collections.emptyList(), classLoader);
        } catch (ProgramInvocationException e) {
            throw e;
        } catch (Exception e) {
            String term = e.getMessage() == null ? "." : (": " + e.getMessage());
            throw new ProgramInvocationException("The program execution failed" + term, e);
        } finally {
            try {
                client.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ClusterClient createClusterClient() throws ProgramInvocationException {
        Configuration configuration = new Configuration();
        configuration.setString(HighAvailabilityOptions.HA_MODE,
            HighAvailabilityMode.ZOOKEEPER.toString().toLowerCase());
        configuration.setString(HighAvailabilityOptions.HA_CLUSTER_ID, "daily");
        configuration.setString(HighAvailabilityOptions.HA_ZOOKEEPER_QUORUM,
            "10.1.22.21:2181,10.1.22.22:2181,10.1.22.23:2181");
        configuration.setString(HighAvailabilityOptions.HA_STORAGE_PATH,
            "hdfs://sunset002.daily.2dfire.info:8020/flink/ha");
        configuration.setString(JobManagerOptions.ADDRESS, "10.1.21.95");
        configuration.setInteger(JobManagerOptions.PORT, 6123);

        ClusterClient client;
        try {
            client = new StandaloneClusterClient(configuration);
            client.setPrintStatusDuringExecution(true);
            client.setDetached(true);
        } catch (Exception e) {
            throw new ProgramInvocationException("Cannot establish connection to JobManager: " + e.getMessage(), e);
        }
        return client;
    }

}
