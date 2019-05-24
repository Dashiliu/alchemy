package com.dfire.platform.alchemy.web.cluster.flink;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.dfire.platform.alchemy.function.*;
import com.dfire.platform.alchemy.web.cluster.ClusterInfo;
import com.dfire.platform.alchemy.web.common.Alias;
import com.dfire.platform.alchemy.web.descriptor.SourceDescriptor;
import com.dfire.platform.alchemy.web.side.SideParser;
import com.dfire.platform.alchemy.web.side.SideStream;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.JobSubmissionResult;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.JobWithJars;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.graph.StreamGraph;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.functions.AggregateFunction;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.table.sinks.TableSink;
import org.apache.flink.table.sources.TableSource;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dfire.platform.alchemy.web.cluster.Handler;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.common.ResultMessage;
import com.dfire.platform.alchemy.web.descriptor.Descriptor;
import com.dfire.platform.alchemy.web.util.AlchemyProperties;
import com.dfire.platform.alchemy.web.util.FileUtils;
import com.dfire.platform.alchemy.web.util.MavenJarUtils;
import com.dfire.platform.alchemy.web.util.PropertiesUtils;

/**
 * @author congbai
 * @date 2019/5/15
 */
public class SubmitSqlHandler implements Handler<SqlSubmitFlinkRequest, SubmitFlinkResponse> {

    private static final String MATCH_CODE = "^\\$\\{[0-9]{1,2}\\}$";

    private static final String REPLACE_CODE = "[^\\d]+";

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmitSqlHandler.class);


    private final ClusterClient clusterClient;

    private final ClusterInfo clusterInfo;

    public SubmitSqlHandler(ClusterClient clusterClient, ClusterInfo clusterInfo) {
        this.clusterClient = clusterClient;
        this.clusterInfo = clusterInfo;
    }

    @Override
    public SubmitFlinkResponse handle(SqlSubmitFlinkRequest request) throws Exception {
        LOGGER.trace("start submit sql request,jobName:{},sql:{}", request.getJobName(), request.getTable().getSql());
        if (CollectionUtils.isEmpty(request.getTable().getSources())) {
            return new SubmitFlinkResponse(ResultMessage.SOURCE_EMPTY.getMsg());
        }
        if (CollectionUtils.isEmpty(request.getTable().getSinkDescriptors())) {
            return new SubmitFlinkResponse(ResultMessage.SINK_EMPTY.getMsg());
        }
        if (StringUtils.isEmpty(request.getTable().getSql())) {
            return new SubmitFlinkResponse(ResultMessage.SQL_EMPTY.getMsg());
        }
        final StreamExecutionEnvironment execEnv = StreamExecutionEnvironment.createLocalEnvironment();
        StreamTableEnvironment env = StreamTableEnvironment.getTableEnvironment(execEnv);
        List<URL> urls = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getAvgs())) {
            //先加入外部依赖，方便发现函数的class
            urls.addAll(FileUtils.createPath(request.getAvgs()));
        }
        Map<String, SourceDescriptor> sideSources = Maps.newHashMap();
        Map<String, TableSource> tableSources = Maps.newHashMap();
        setBaseInfo(execEnv , request);
        registerSource(env ,  request , urls ,tableSources , sideSources);
        registerFunction(env , request ,urls);
        Table table = registerSql(env , request ,tableSources , sideSources);
        registerSink(table , request , urls);
        if (request.isTest()) {
            execEnv.execute(request.getJobName());
            return new SubmitFlinkResponse(true, "");
        }
        StreamGraph streamGraph = execEnv.getStreamGraph();
        streamGraph.setJobName(request.getJobName());
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

    private void registerSink(Table table, SqlSubmitFlinkRequest request, List<URL> urls) {
        request.getTable().getSinkDescriptors().forEach(sinkDescriptor -> {
            try {
                replaceCodeValue(request, sinkDescriptor);
                TableSink tableSink = sinkDescriptor.transform(ClusterType.FLINK);
                table.writeToSink(tableSink);
                addUrl(sinkDescriptor.getType(), urls);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Table registerSql(StreamTableEnvironment env, SqlSubmitFlinkRequest request, Map<String, TableSource> tableSources, Map<String, SourceDescriptor> sideSources ) throws Exception {
        if (sideSources.isEmpty()){
            return env.sqlQuery(request.getTable().getSql());
        }else{
            Deque<SqlNode> deque = SideParser.parse(request.getTable().getSql());
            SqlNode last = deque.pollLast();
            SqlSelect modifyNode = null;
            SqlNode fullNode = deque.peekFirst();
            while (true) {
                if (last.getKind() == SqlKind.SELECT) {
                    SqlSelect sqlSelect = (SqlSelect) last;
                    SqlNode node = sqlSelect.getFrom();
                    if (SqlKind.JOIN != node.getKind()){
                        continue;
                    }
                    SqlJoin sqlJoin = (SqlJoin) node;
                    Alias sideAlias = SideParser.getTableName(sqlJoin.getRight());
                    Alias leftAlias = SideParser.getTableName(sqlJoin.getLeft());
                    if (isSide(sideSources.keySet() , leftAlias.getTable())){
                        throw new UnsupportedOperationException("side table must be right table");
                    }
                    if(!isSide(sideSources.keySet() , sideAlias.getTable())){
                        continue;
                    }
                    DataStream<Row> dataStream = SideStream.buildStream( env, sqlSelect, leftAlias, sideAlias, sideSources.get(sideAlias.getTable()));
                    Alias newTable = new Alias(leftAlias.getTable()+"_"+sideAlias.getTable(),leftAlias.getAlias()+"_"+sideAlias.getAlias());
                    if (!env.isRegistered(newTable.getTable())){
                        env.registerDataStream(newTable.getTable(), dataStream);
                    }
                    SqlSelect newSelect = SideParser.newSelect(sqlSelect , newTable.getTable() , newTable.getAlias(),false ,true);
                    modifyNode = newSelect;
                }
                SqlNode node = deque.pollLast();
                if (node == null){
                    if (modifyNode != null){
                        fullNode = modifyNode;
                    }
                    break;
                }else{
                    last = node;
                    if (modifyNode != null){
                        SideParser.rewrite(node,modifyNode);
                        modifyNode = null;
                    }
                }
            }
            return env.sqlQuery(fullNode.toString());
        }
    }

    private boolean isSide(Set<String> keySet, String table) {
        for (String side : keySet){
            if (side.equalsIgnoreCase(table)){
                return true;
            }
        }
        return false;
    }

    private void registerFunction(StreamTableEnvironment env, SqlSubmitFlinkRequest request, List<URL> urls) {
        if (CollectionUtils.isNotEmpty(request.getTable().getUdfs())) {
            request.getTable().getUdfs().forEach(udfDescriptor -> {
                try {
                    addUrl(udfDescriptor.getType(), urls);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            List<String> functionNames = Lists.newArrayList();
            loadFunction(env, functionNames, ServiceLoader.load(BaseFunction.class));
            URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]) , Thread.currentThread().getContextClassLoader());
            loadFunction(env, functionNames, ServiceLoader.load(BaseFunction.class , urlClassLoader));
            request.getTable().getUdfs().forEach(udfDescriptor -> {
                try {
                    if (StringUtils.isEmpty(udfDescriptor.getValue()) || functionNames.contains(udfDescriptor.getName())){
                        return;
                    }
                    if (udfDescriptor.getValue().matches(MATCH_CODE)){
                        replaceCodeValue(request, udfDescriptor);
                        BaseFunction udf = udfDescriptor.transform(ClusterType.FLINK);
                        register(env, udf);
                    }else{
                        Class clazz = Class.forName(udfDescriptor.getValue() ,false , urlClassLoader);
                        Object udf = clazz.newInstance();
                        register(env , (BaseFunction) udf);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });
        }
    }

    private void loadFunction(StreamTableEnvironment env, List<String> functionNames, ServiceLoader<BaseFunction> serviceLoader) {
        Iterator<BaseFunction> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            BaseFunction function = iterator.next();
            if (StringUtils.isEmpty(function.getFunctionName()) || functionNames.contains(function.getFunctionName())) {
                continue;
            }
            register(env , function);
        }
    }

    private void register(StreamTableEnvironment env, BaseFunction function) {
        if (function instanceof TableFunction) {
            env.registerFunction(function.getFunctionName(), (TableFunction)function);
        } else if (function instanceof AggregateFunction) {
            env.registerFunction(function.getFunctionName(), (AggregateFunction)function);
        } else if (function instanceof ScalarFunction) {
            env.registerFunction(function.getFunctionName(), (ScalarFunction)function);
        } else {
            throw new RuntimeException("Unknown UDF {} was found." + function.getFunctionName());
        }
    }

    private void registerSource(StreamTableEnvironment env, SqlSubmitFlinkRequest request, List<URL> urls, Map<String, TableSource> tableSources, Map<String, SourceDescriptor> sideSources) {
        request.getTable().getSources().forEach(consumer -> {
            try {
                if (consumer.getSide() !=null ){
                    sideSources.put(consumer.getName() , consumer);
                    return;
                }
                TableSource tableSource = consumer.transform(ClusterType.FLINK);
                addUrl(consumer.getConnectorDescriptor().getType(), urls);
                if (consumer.getFormat() != null){
                    addUrl(consumer.getFormat().getType(), urls);
                }
                env.registerTableSource(consumer.getName(), tableSource);
                tableSources.put(consumer.getName() , tableSource);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setBaseInfo(StreamExecutionEnvironment execEnv,SqlSubmitFlinkRequest request) {
        execEnv.setParallelism(request.getParallelism());
        execEnv.setRestartStrategy(RestartStrategies.fixedDelayRestart(
            PropertiesUtils.getProperty(request.getRestartAttempts(), Constants.RESTART_ATTEMPTS),
            PropertiesUtils.getProperty(request.getDelayBetweenAttempts(), Constants.DELAY_BETWEEN_ATTEMPTS)));
        if (request.getCheckpointingInterval() != null) {
            execEnv.enableCheckpointing(request.getCheckpointingInterval());
        }
        if (StringUtils.isNotEmpty(request.getTimeCharacteristic())) {
            execEnv.setStreamTimeCharacteristic(TimeCharacteristic.valueOf(request.getTimeCharacteristic()));
        } else {
            execEnv.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        }
    }

    private void addUrl(String name, List<URL> urls) throws MalformedURLException {
        if (StringUtils.isEmpty(name)) {
            return;
        }
        String avg = AlchemyProperties.get(name);
        if (StringUtils.isEmpty(avg)) {
            LOGGER.info("{} is not exist  in alchemy properties", name);
            return;
        }
        URL url = MavenJarUtils.forAvg(avg).getJarFile().getAbsoluteFile().toURI().toURL();
        if (!urls.contains(url)) {
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
                String value = (String)val;
                Pattern pattern = Pattern.compile(REPLACE_CODE);
                Matcher matcher = pattern.matcher(value);
                Integer index = Integer.valueOf(matcher.replaceAll("").trim());
                field.set(descriptor, message.getTable().getCodes().get(index));
            }
        }
    }

    private List<URL> createGlobalPath() throws MalformedURLException {
        return FileUtils.createPath(this.clusterInfo.getAvgs());
    }
}
