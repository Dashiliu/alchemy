package com.dfire.platform.alchemy.web.cluster;

import com.dfire.platform.alchemy.web.cluster.request.*;
import com.dfire.platform.alchemy.web.cluster.response.JobStatusResponse;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.cluster.response.SubmitFlinkResponse;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.common.ResultMessage;
import com.dfire.platform.alchemy.web.common.Status;
import com.dfire.platform.alchemy.web.descriptor.Descriptor;
import com.dfire.platform.alchemy.web.util.AlchemyProperties;
import com.dfire.platform.alchemy.web.util.JarArgUtils;
import com.dfire.platform.alchemy.web.util.MavenJarUtils;
import com.dfire.platform.alchemy.web.util.PropertiesUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobSubmissionResult;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.JobWithJars;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.StandaloneClusterClient;
import org.apache.flink.configuration.AkkaOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.HighAvailabilityOptions;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.optimizer.DataStatistics;
import org.apache.flink.optimizer.Optimizer;
import org.apache.flink.optimizer.costs.DefaultCostEstimator;
import org.apache.flink.optimizer.plan.FlinkPlan;
import org.apache.flink.runtime.jobgraph.JobStatus;
import org.apache.flink.runtime.jobmanager.HighAvailabilityMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.graph.StreamGraph;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.functions.AggregateFunction;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.table.sinks.TableSink;
import org.apache.flink.table.sources.TableSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author congbai
 * @date 01/06/2018
 */
public class FlinkCluster implements Cluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkCluster.class);

    private static final String MATCH_CODE = "^\\$\\{[0-9]{1,2}\\}$";

    private static final String REPLACE_CODE = "[^\\d]+";

    private ClusterClient clusterClient;

    private ClusterInfo clusterInfo;

    @Override
    public Cluster newInstance() {
        return new FlinkCluster();
    }

    @Override
    public ClusterType clusterType() {
        return ClusterType.FLINK;
    }

    @Override
    public void start(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
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
            return sendSqlSubmitRequest((SqlSubmitFlinkRequest) message);
        } else if (message instanceof JarSubmitFlinkRequest) {
            return sendJarSubmitRequest((JarSubmitFlinkRequest) message);
        } else if (message instanceof CancelFlinkRequest) {
            return cancelJob((CancelFlinkRequest) message);
        } else if (message instanceof JobStatusRequest) {
            return getJobStatus((JobStatusRequest) message);
        } else {
            throw new UnsupportedOperationException("unknow message type:" + message.getClass().getName());
        }
    }

    private Response getJobStatus(JobStatusRequest message) throws Exception {
        CompletableFuture<JobStatus> jobStatusCompletableFuture
            = clusterClient.getJobStatus(JobID.fromHexString(message.getJobID()));
        // jobStatusCompletableFuture.
        switch (jobStatusCompletableFuture.get()) {
            case CREATED:
            case RESTARTING:
                break;
            case RUNNING:
                return new JobStatusResponse(true, Status.RUNNING.getStatus());
            case FAILING:
            case FAILED:
                return new JobStatusResponse(true, Status.FAILED.getStatus());
            case CANCELLING:
            case CANCELED:
                return new JobStatusResponse(true, Status.CANCELED.getStatus());
            case FINISHED:
                return new JobStatusResponse(true, Status.FINISHED.getStatus());
            case SUSPENDED:
            case RECONCILING:
            default:
                // nothing to do
        }
        return new JobStatusResponse(null);
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
            File file = MavenJarUtils.forAvg(message.getJarInfoDescriptor().getAvg()).getJarFile();
            List<String> programArgs = JarArgUtils.tokenizeArguments(message.getJarInfoDescriptor().getProgramArgs());
            PackagedProgram program = new PackagedProgram(file, message.getJarInfoDescriptor().getEntryClass(),
                programArgs.toArray(new String[programArgs.size()]));
            ClassLoader classLoader = null;
            try {
                classLoader = program.getUserCodeClassLoader();
            } catch (Exception e) {
                LOGGER.warn(e.getMessage());
            }

            Optimizer optimizer = new Optimizer(new DataStatistics(), new DefaultCostEstimator(), new Configuration());
            FlinkPlan plan
                = ClusterClient.getOptimizedPlan(optimizer, program, message.getJarInfoDescriptor().getParallelism());
            // set up the execution environment
            List<URL> jarFiles = createPath(file);
            JobSubmissionResult submissionResult
                = clusterClient.run(plan, jarFiles, Collections.emptyList(), classLoader);
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
        List<URL> urls =new ArrayList<>();
        message.getTable().getSources().forEach(consumer -> {
            try {
                TableSource tableSource = consumer.transform(clusterType());
                addUrl(consumer.getConnectorDescriptor().getType() , urls);
                env.registerTableSource(consumer.getName(), tableSource);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        if (CollectionUtils.isNotEmpty(message.getTable().getUdfs())) {
            message.getTable().getUdfs().forEach(udfDescriptor -> {
                Object udf;
                try {
                    replaceCodeValue(message, udfDescriptor);
                    udf = udfDescriptor.transform(clusterType());
                    if (udf instanceof TableFunction) {
                        env.registerFunction(udfDescriptor.getName(), (TableFunction) udf);
                    } else if (udf instanceof AggregateFunction) {
                        env.registerFunction(udfDescriptor.getName(), (AggregateFunction) udf);
                    } else if (udf instanceof ScalarFunction) {
                        env.registerFunction(udfDescriptor.getName(), (ScalarFunction) udf);
                    } else {
                        throw new RuntimeException("Unknown UDF {} was found."+udf.getClass().getName());
                    }
                    addUrl(udfDescriptor.getType() , urls);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });
        }

        Table table = env.sqlQuery(message.getTable().getSql());
        message.getTable().getSinkDescriptors().forEach(sinkDescriptor -> {
            try {
                replaceCodeValue(message, sinkDescriptor);
                TableSink tableSink = sinkDescriptor.transform(clusterType());
                table.writeToSink(tableSink);
                addUrl(sinkDescriptor.getType() , urls);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        if (message.isTest()) {
            execEnv.execute(message.getJobName());
            return new SubmitFlinkResponse(true, "");
        }
        StreamGraph streamGraph = execEnv.getStreamGraph();
        streamGraph.setJobName(message.getJobName());
        if (CollectionUtils.isNotEmpty(message.getAvgs())) {
            urls.addAll(createPath(message.getAvgs()));
        }
        urls.addAll(createGlobalPath());
        ClassLoader usercodeClassLoader
            = JobWithJars.buildUserCodeClassLoader(urls, Collections.emptyList(), getClass().getClassLoader());
        try {
            JobSubmissionResult submissionResult
                = clusterClient.run(streamGraph, urls, Collections.emptyList(), usercodeClassLoader);
            LOGGER.trace(" submit sql request success,jobId:{}", submissionResult.getJobID());
            return new SubmitFlinkResponse(true, submissionResult.getJobID().toString());
        } catch (Exception e) {
            String term = e.getMessage() == null ? "." : (": " + e.getMessage());
            LOGGER.error(" submit sql request fail", e);
            return new SubmitFlinkResponse(term);
        }
    }

    private  void addUrl(String name , List<URL> urls) throws MalformedURLException {
        if (StringUtils.isEmpty(name)){
            return;
        }
        String avg = AlchemyProperties.get(name);
        if (StringUtils.isEmpty(avg)) {
            LOGGER.info("{} is not exist  in alchemy properties" , name);
            return;
        }
        URL url = MavenJarUtils.forAvg(avg).getJarFile().getAbsoluteFile().toURI().toURL();
        if (!urls.contains(url)){
            urls.add(url);
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
                String value = (String) val;
                if (value.matches(MATCH_CODE)) {
                    Pattern pattern = Pattern.compile(REPLACE_CODE);
                    Matcher matcher = pattern.matcher(value);
                    Integer index = Integer.valueOf(matcher.replaceAll("").trim());
                    field.set(descriptor, message.getTable().getCodes().get(index));
                }
            }
        }
    }

    private List<URL> createPath(List<String> avgs) throws MalformedURLException {
        List<URL> jarFiles = new ArrayList<>(avgs.size());
        for (String avg : avgs){
            try {
                URL jarFileUrl =  MavenJarUtils.forAvg(avg).getJarFile().getAbsoluteFile().toURI().toURL();
                jarFiles.add(jarFileUrl);
                JobWithJars.checkJarFile(jarFileUrl);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("avg is invalid '" +avg + "'", e);
            } catch (IOException e) {
                throw new RuntimeException("Problem with avg " + avg, e);
            }
        }
        return jarFiles;
    }

    private List<URL> createPath(File file) {
        List<URL> jarFiles = new ArrayList<>(1);
        if (file == null) {
            return jarFiles;
        }
        try {

            URL jarFileUrl = file.getAbsoluteFile().toURI().toURL();
            jarFiles.add(jarFileUrl);
            JobWithJars.checkJarFile(jarFileUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("JAR file is invalid '" + file.getAbsolutePath() + "'", e);
        } catch (IOException e) {
            throw new RuntimeException("Problem with jar file " + file.getAbsolutePath(), e);
        }
        return jarFiles;
    }

    private List<URL> createGlobalPath() throws MalformedURLException {
        if (CollectionUtils.isEmpty(this.clusterInfo.getAvgs())) {
            return Collections.emptyList();
        }
        return createPath(this.clusterInfo.getAvgs());
    }

    @Override
    public void destroy() throws Exception {
        clusterClient.shutdown();
    }

}
