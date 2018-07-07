package com.dfire.platform.alchemy.web.cluster;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobSubmissionResult;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.JobWithJars;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.StandaloneClusterClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.HighAvailabilityOptions;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.optimizer.DataStatistics;
import org.apache.flink.optimizer.Optimizer;
import org.apache.flink.optimizer.costs.DefaultCostEstimator;
import org.apache.flink.optimizer.plan.FlinkPlan;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.apache.flink.runtime.instance.ActorGateway;
import org.apache.flink.runtime.jobmanager.HighAvailabilityMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.graph.StreamGraph;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.sources.TableSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dfire.platform.alchemy.api.function.aggregate.FlinkAllAggregateFunction;
import com.dfire.platform.alchemy.api.function.scalar.FlinkAllScalarFunction;
import com.dfire.platform.alchemy.api.function.table.FlinkAllTableFunction;
import com.dfire.platform.alchemy.web.cluster.request.*;
import com.dfire.platform.alchemy.web.cluster.response.ListJobFlinkResponse;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.cluster.response.SubmitFlinkResponse;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.common.ResultMessage;
import com.dfire.platform.alchemy.web.descriptor.Descriptor;
import com.dfire.platform.alchemy.web.util.PropertiesUtils;

/**
 * @author congbai
 * @date 01/06/2018
 */
@Component
public class FlinkDefaultCluster implements Cluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkDefaultCluster.class);

    private static final String MATCH_CODE = "^\\$\\{[0-9]{1,2}\\}$";

    private static final String REPLACE_CODE = "[^\\d]+";

    private ClusterClient clusterClient;

    private String globalClassPath;

    @Override
    public String name() {
        return Constants.DEFAULT_FLINK_CLUSTER;
    }

    @Override
    public ClusterType clusterType() {
        return ClusterType.FLINK;
    }

    @Override
    public void start(ClusterInfo clusterInfo) {
        this.globalClassPath = clusterInfo.getGlobalClassPath();
        Configuration configuration = new Configuration();
        configuration.setString(HighAvailabilityOptions.HA_MODE,
            HighAvailabilityMode.ZOOKEEPER.toString().toLowerCase());
        configuration.setString(HighAvailabilityOptions.HA_CLUSTER_ID, clusterInfo.getClusterId());
        configuration.setString(HighAvailabilityOptions.HA_ZOOKEEPER_QUORUM, clusterInfo.getZookeeperQuorum());
        configuration.setString(HighAvailabilityOptions.HA_STORAGE_PATH, clusterInfo.getStoragePath());
        configuration.setString(JobManagerOptions.ADDRESS, clusterInfo.getAddress());
        configuration.setInteger(JobManagerOptions.PORT, clusterInfo.getPort());
        try {
            this.clusterClient = new StandaloneClusterClient(configuration);
            this.clusterClient.setPrintStatusDuringExecution(true);
            this.clusterClient.setDetached(true);
        } catch (Exception e) {
            throw new RuntimeException("Cannot establish connection to JobManager: " + e.getMessage(), e);
        }
    }

    @Override
    public Response send(Request message) throws Exception {
        if (message instanceof SqlSubmitFlinkRequest) {
            return sendSqlSubmitRequest((SqlSubmitFlinkRequest)message);
        } else if (message instanceof JarSubmitFlinkRequest) {
            return sendJarSubmitRequest((JarSubmitFlinkRequest)message);
        } else if (message instanceof CancelFlinkRequest) {
            return cancelJob((CancelFlinkRequest)message);
        } else if (message instanceof ListJobFlinkRequest) {
            return listJob((ListJobFlinkRequest)message);
        } else {
            throw new UnsupportedOperationException("unknow message type:" + message.getClass().getName());
        }
    }

    private Response listJob(ListJobFlinkRequest message) throws Exception {
        Collection<JobStatusMessage> jobStatusMessages = clusterClient.listJobs().get();
        return new ListJobFlinkResponse(true, jobStatusMessages);
    }

    private Response cancelJob(CancelFlinkRequest message) throws Exception {
        clusterClient.cancel(JobID.fromHexString(message.getJobID()));
        return new Response(true);
    }

    private Response sendJarSubmitRequest(JarSubmitFlinkRequest message) throws Exception {
        if (message.isTest()) {
            throw new UnsupportedOperationException();
        }
        LOGGER.trace("start submit jar request,entryClass:{}", message.getJarInfoDescriptor().getEntryClass());
        try {
            PackagedProgram program = new PackagedProgram(new File(message.getJarInfoDescriptor().getJarPath()),
                message.getJarInfoDescriptor().getEntryClass(), message.getJarInfoDescriptor().getProgramArgs());
            ClassLoader classLoader = null;
            try {
                classLoader = program.getUserCodeClassLoader();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Optimizer optimizer = new Optimizer(new DataStatistics(), new DefaultCostEstimator(), new Configuration());
            FlinkPlan plan
                = ClusterClient.getOptimizedPlan(optimizer, program, message.getJarInfoDescriptor().getParallelism());
            // set up the execution environment
            List<URL> jarFiles = createPath(message.getJarInfoDescriptor().getJarPath());
            JobSubmissionResult submissionResult = clusterClient.run(plan, jarFiles, createGlobalPath(), classLoader);
            LOGGER.trace(" submit jar request sucess,jobId:{}", submissionResult.getJobID());
            return new SubmitFlinkResponse(true, submissionResult.getJobID().toString());
        } catch (Exception e) {
            String term = e.getMessage() == null ? "." : (": " + e.getMessage());
            LOGGER.error(" submit jar request fail", e);
            return new SubmitFlinkResponse(term);
        }
    }

    private Response sendSqlSubmitRequest(SqlSubmitFlinkRequest message) throws Exception {
        LOGGER.trace("start submit sql request,jobName:{},sql:{}", message.getJobName(), message.getTable().getSql());
        if (CollectionUtils.isEmpty(message.getTable().getSources())) {
            return new SubmitFlinkResponse(ResultMessage.SOURCE_EMPTY.getMsg());
        }
        if (CollectionUtils.isEmpty(message.getTable().getSinkDescriptors())) {
            return new SubmitFlinkResponse(ResultMessage.SINK_EMPTY.getMsg());
        }
        if (StringUtils.isEmpty(message.getTable().getSql())) {
            return new SubmitFlinkResponse(ResultMessage.SQL_EMPTY.getMsg());
        }
        final StreamExecutionEnvironment execEnv = StreamExecutionEnvironment.createLocalEnvironment();
        execEnv.setParallelism(message.getParallelism());
        execEnv.setRestartStrategy(RestartStrategies.fixedDelayRestart(
            PropertiesUtils.getProperty(message.getRestartAttempts(), Constants.RESTART_ATTEMPTS),
            PropertiesUtils.getProperty(message.getDelayBetweenAttempts(), Constants.DELAY_BETWEEN_ATTEMPTS)));
        if (message.getCheckpointingInterval() != null) {
            execEnv.enableCheckpointing(message.getCheckpointingInterval());
        }
        StreamTableEnvironment env = StreamTableEnvironment.getTableEnvironment(execEnv);
        if (StringUtils.isNotEmpty(message.getTimeCharacteristic())) {
            execEnv.setStreamTimeCharacteristic(TimeCharacteristic.valueOf(message.getTimeCharacteristic()));
        } else {
            execEnv.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        }
        message.getTable().getSources().forEach(consumer -> {
            try {
                TableSource tableSource = consumer.transform(clusterType());
                env.registerTableSource(consumer.getName(), tableSource);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        if (CollectionUtils.isNotEmpty(message.getTable().getUdfs())) {
            message.getTable().getUdfs().forEach(udfDescriptor -> {
                Object udf = null;
                try {
                    replaceCodeValue(message, udfDescriptor);
                    udf = udfDescriptor.transform(clusterType());
                    if (udf instanceof FlinkAllTableFunction) {
                        env.registerFunction(udfDescriptor.getName(), (FlinkAllTableFunction)udf);
                    } else if (udf instanceof FlinkAllAggregateFunction) {
                        env.registerFunction(udfDescriptor.getName(), (FlinkAllAggregateFunction)udf);
                    } else if (udf instanceof FlinkAllScalarFunction) {
                        env.registerFunction(udfDescriptor.getName(), (FlinkAllScalarFunction)udf);
                    } else {
                        LOGGER.warn("Unknown UDF {} was found.", udf.getClass().getName());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });
        }
        Table table = env.sqlQuery(message.getTable().getSql());
        message.getTable().getSinkDescriptors().forEach(sinkDescriptor -> {
            try {
                replaceCodeValue(message, sinkDescriptor);
                table.writeToSink(sinkDescriptor.transform(clusterType()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
        if (message.isTest()) {
            execEnv.execute(message.getJobName());
        }
        StreamGraph streamGraph = execEnv.getStreamGraph();
        streamGraph.setJobName(message.getJobName());
        List<URL> jarFiles = createPath(message.getJarPath());
        jarFiles.add(new File(Constants.FILE_PATH+Constants.GLOBAL_FILE_NAME).getAbsoluteFile().toURI().toURL());
        ClassLoader usercodeClassLoader
            = JobWithJars.buildUserCodeClassLoader(jarFiles, createGlobalPath(), getClass().getClassLoader());
        try {
            JobSubmissionResult submissionResult
                = clusterClient.run(streamGraph, jarFiles, Collections.emptyList(), usercodeClassLoader);
            LOGGER.trace(" submit sql request success,jobId:{}", submissionResult.getJobID());
            return new SubmitFlinkResponse(true, submissionResult.getJobID().toString());
        } catch (Exception e) {
            String term = e.getMessage() == null ? "." : (": " + e.getMessage());
            LOGGER.error(" submit sql request fail", e);
            return new SubmitFlinkResponse(term);
        }
    }

    private void replaceCodeValue(SqlSubmitFlinkRequest message, Descriptor descriptor) throws Exception {
        Class clazz = descriptor.getClass();
        Field[] fs = clazz.getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            Field field = fs[i];
            // 设置些属性是可以访问的
            field.setAccessible(true);
            // 得到此属性的值
            Object val = field.get(descriptor);
            if (val == null) {
                continue;
            }
            if (val instanceof String) {
                String value = (String)val;
                if (value.matches(MATCH_CODE)) {
                    Pattern pattern = Pattern.compile(REPLACE_CODE);
                    Matcher matcher = pattern.matcher(value);
                    Integer index = Integer.valueOf(matcher.replaceAll("").trim());
                    field.set(descriptor, message.getTable().getCodes().get(index));
                }
            }
        }
    }

    private List<URL> createPath(String filePath) {
        List<URL> jarFiles = new ArrayList<>(1);
        try {
            if (StringUtils.isEmpty(filePath)) {
                return jarFiles;
            }
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

    private List<URL> createGlobalPath() {
        if (StringUtils.isEmpty(this.globalClassPath)) {
            return Collections.emptyList();
        }
        List<URL> jarFiles = new ArrayList<>(1);
        try {
            URL jarFileUrl = new File(this.globalClassPath).getAbsoluteFile().toURI().toURL();
            jarFiles.add(jarFileUrl);
            JobWithJars.checkJarFile(jarFileUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("globalClasspath is invalid '" + this.globalClassPath + "'", e);
        } catch (IOException e) {
            throw new RuntimeException("Problem with jar file " + this.globalClassPath, e);
        }
        return jarFiles;
    }

    @Override
    public void destroy() throws Exception {
        clusterClient.shutdown();
    }

}
