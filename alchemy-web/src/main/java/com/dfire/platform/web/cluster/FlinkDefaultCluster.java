package com.dfire.platform.web.cluster;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dfire.platform.web.cluster.response.ListJobFlinkResponse;
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
import org.apache.flink.runtime.jobmanager.HighAvailabilityMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.graph.StreamGraph;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.sources.TableSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dfire.platform.api.function.aggregate.FlinkAllAggregateFunction;
import com.dfire.platform.api.function.scalar.FlinkAllScalarFunction;
import com.dfire.platform.api.function.table.FlinkAllTableFunction;
import com.dfire.platform.web.cluster.request.*;
import com.dfire.platform.web.cluster.response.Response;
import com.dfire.platform.web.cluster.response.SubmitFlinkResponse;
import com.dfire.platform.web.common.ClusterType;
import com.dfire.platform.web.common.Constants;
import com.dfire.platform.web.common.ResultMessage;
import com.dfire.platform.web.util.PropertiesUtils;

/**
 * @author congbai
 * @date 01/06/2018
 */
public class FlinkDefaultCluster implements Cluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkDefaultCluster.class);

    private static final String NAME = "flink_default";

    private ClusterClient clusterClient;

    private String globalClassPath;

    @Override
    public String name() {
        return NAME;
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
        return new ListJobFlinkResponse(true,clusterClient.listJobs().get());
    }

    private Response cancelJob(CancelFlinkRequest message) throws Exception {
        clusterClient.cancel(JobID.fromByteArray(message.getJobID().getBytes()));
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
            ClassLoader classLoader = program.getUserCodeClassLoader();

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
        LOGGER.trace("start submit sql request,jobName:{},sql:{}", message.getJobName(),
            message.getSqlInfoDescriptor().getSql());
        if (CollectionUtils.isEmpty(message.getInputs())) {
            return new SubmitFlinkResponse(ResultMessage.SOURCE_EMPTY.getMsg());
        }
        if (CollectionUtils.isEmpty(message.getOutputs())) {
            return new SubmitFlinkResponse(ResultMessage.SINK_EMPTY.getMsg());
        }
        if (StringUtils.isEmpty(message.getSqlInfoDescriptor().getSql())) {
            return new SubmitFlinkResponse(ResultMessage.SQL_EMPTY.getMsg());
        }
        final StreamExecutionEnvironment execEnv = StreamExecutionEnvironment.createLocalEnvironment();
        execEnv.setParallelism(message.getSqlInfoDescriptor().getParallelism());
        execEnv.setRestartStrategy(RestartStrategies.fixedDelayRestart(
            PropertiesUtils.getProperty(message.getSqlInfoDescriptor().getRestartAttempts(),
                Constants.RESTART_ATTEMPTS),
            PropertiesUtils.getProperty(message.getSqlInfoDescriptor().getDelayBetweenAttempts(),
                Constants.DELAY_BETWEEN_ATTEMPTS)));
        if (message.getSqlInfoDescriptor().getCheckpointingInterval() != null) {
            execEnv.enableCheckpointing(message.getSqlInfoDescriptor().getCheckpointingInterval());
        }
        StreamTableEnvironment env = StreamTableEnvironment.getTableEnvironment(execEnv);
        if (StringUtils.isNotEmpty(message.getSqlInfoDescriptor().getTimeCharacteristic())) {
            execEnv.setStreamTimeCharacteristic(
                TimeCharacteristic.valueOf(message.getSqlInfoDescriptor().getTimeCharacteristic()));
        } else {
            execEnv.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        }
        message.getInputs().forEach(consumer -> {
            try {
                TableSource tableSource = consumer.transform(clusterType());
                env.registerTableSource(consumer.getName(), tableSource);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        if (CollectionUtils.isNotEmpty(message.getUserDefineFunctions())) {
            message.getUserDefineFunctions().forEach(consumer -> {
                Object udf = null;
                try {
                    udf = consumer.transform(clusterType());
                    if (udf instanceof FlinkAllTableFunction) {
                        env.registerFunction(consumer.getName(), (FlinkAllTableFunction)udf);
                    } else if (udf instanceof FlinkAllAggregateFunction) {
                        env.registerFunction(consumer.getName(), (FlinkAllAggregateFunction)udf);
                    } else if (udf instanceof FlinkAllScalarFunction) {
                        env.registerFunction(consumer.getName(), (FlinkAllScalarFunction)udf);
                    } else {
                        LOGGER.warn("Unknown UDF {} was found.", udf.getClass().getName());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });
        }
        Table table = env.sqlQuery(message.getSqlInfoDescriptor().getSql());
        message.getOutputs().forEach(consumer -> {
            try {
                table.writeToSink(consumer.transform(clusterType()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
        if (message.isTest()) {
            execEnv.execute(message.getJobName());
        }
        StreamGraph streamGraph = execEnv.getStreamGraph();
        streamGraph.setJobName(message.getJobName());
        List<URL> jarFiles = createPath(message.getSqlInfoDescriptor().getJarPath());
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

    private List<URL> createPath(String filePath) {
        List<URL> jarFiles = new ArrayList<>(1);
        if (StringUtils.isEmpty(filePath)) {
            return jarFiles;
        }
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
