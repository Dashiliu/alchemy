package com.dfire.platform.alchemy.connectors.elasticsearch;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.elasticsearch.ActionRequestFailureHandler;
import org.apache.flink.streaming.connectors.elasticsearch.util.NoOpFailureHandler;
import org.apache.flink.streaming.connectors.elasticsearch.util.RetryRejectedExecutionFailureHandler;
import org.apache.flink.streaming.connectors.elasticsearch5.ElasticsearchSink;
import org.apache.flink.table.sinks.AppendStreamTableSink;
import org.apache.flink.table.sinks.TableSink;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;

/**
 * @author congbai
 * @date 05/06/2018
 */
public class ElasticsearchTableSink implements AppendStreamTableSink<Row> {

    private final ElasticsearchProperties elasticsearchProperties;

    private String[] fieldNames;

    private TypeInformation[] fieldTypes;

    private JsonRowStringSchema jsonRowSchema;

    public ElasticsearchTableSink(ElasticsearchProperties elasticsearchProperties) {
        this.elasticsearchProperties = elasticsearchProperties;
    }

    @Override
    public TypeInformation<Row> getOutputType() {
        return new RowTypeInfo(getFieldTypes());
    }

    @Override
    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public TypeInformation<?>[] getFieldTypes() {
        return fieldTypes;
    }

    @Override
    public TableSink<Row> configure(String[] fieldNames, TypeInformation<?>[] fieldTypes) {
        ElasticsearchTableSink copy = new ElasticsearchTableSink(this.elasticsearchProperties);
        copy.fieldNames = Preconditions.checkNotNull(fieldNames, "fieldNames");
        copy.fieldTypes = Preconditions.checkNotNull(fieldTypes, "fieldTypes");
        Preconditions.checkArgument(fieldNames.length == fieldTypes.length,
            "Number of provided field names and types does not match.");

        RowTypeInfo rowSchema = new RowTypeInfo(fieldTypes, fieldNames);
        copy.jsonRowSchema = new JsonRowStringSchema(rowSchema);

        return copy;
    }

    private ElasticsearchSink<Row> createEsSink() {
        Map<String, String> userConfig = createUserConfig();
        List<InetSocketAddress> transports = new ArrayList<>();
        addTransportAddress(transports, this.elasticsearchProperties.getTransports());
        ActionRequestFailureHandler actionRequestFailureHandler = createFailureHandler(this.elasticsearchProperties.getFailureHandler());
        return new ElasticsearchSink<>(userConfig, transports,
            new ElasticsearchTableFunction(this.elasticsearchProperties.getIndex(), this.elasticsearchProperties.getIndexField(), this.elasticsearchProperties.getDateFormat(), this.jsonRowSchema, this.fieldNames),
                actionRequestFailureHandler);
    }

    private ActionRequestFailureHandler createFailureHandler(String failureHandler) {
        if(failureHandler == null || failureHandler.trim().length() == 0){
            return new NoOpFailureHandler();
        }
        FailureHandler handler = FailureHandler.valueOf(failureHandler.toUpperCase());
        switch (handler){
            case IGNORE:
                return new IgnoreFailureHandler();
            case RETRYREJECTED:
                return new RetryRejectedExecutionFailureHandler();
            default:
                return new NoOpFailureHandler();
        }
    }

    private Map<String, String> createUserConfig() {
        Map<String, String> userConfig  =new HashMap<>();
        userConfig.put("cluster.name", this.elasticsearchProperties.getClusterName());
        Map<String, Object> config = this.elasticsearchProperties.getConfig();
        if (config == null){
            return userConfig;
        }
        for(Map.Entry<String, Object> entry : config.entrySet()){
            userConfig.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return userConfig;
    }

    private void addTransportAddress(List<InetSocketAddress> transports, String serverList) {
        for (String server : serverList.split(",")) {
            try {
                String[] array = server.split(":");
                String host = array[0];
                int port = Integer.parseInt(array[1]);
                transports.add(new InetSocketAddress(InetAddress.getByName(host), port));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void emitDataStream(DataStream<Row> dataStream) {
        ElasticsearchSink<Row> elasticsearchSink = createEsSink();
        dataStream.addSink(elasticsearchSink);
    }
}
