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

    private final String address;

    private final String clusterName;

    private final String index;

    private final int bufferSize;

    private final String filedIndex;

    private final String formatDate;

    private String[] fieldNames;

    private TypeInformation[] fieldTypes;

    private JsonRowStringSchema jsonRowSchema;

    public ElasticsearchTableSink(String address, String clusterName, String index, int bufferSize, String filedIndex,
        String formatDate) {
        this.address = address;
        this.clusterName = clusterName;
        this.index = index;
        this.bufferSize = bufferSize;
        this.filedIndex = filedIndex;
        this.formatDate = formatDate;
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
        ElasticsearchTableSink copy = new ElasticsearchTableSink(this.address, this.clusterName, this.index,
            this.bufferSize, this.filedIndex, this.formatDate);
        copy.fieldNames = Preconditions.checkNotNull(fieldNames, "fieldNames");
        copy.fieldTypes = Preconditions.checkNotNull(fieldTypes, "fieldTypes");
        Preconditions.checkArgument(fieldNames.length == fieldTypes.length,
            "Number of provided field names and types does not match.");

        RowTypeInfo rowSchema = new RowTypeInfo(fieldTypes, fieldNames);
        copy.jsonRowSchema = new JsonRowStringSchema(rowSchema);

        return copy;
    }

    private ElasticsearchSink<Row> createEsSink() {
        Map<String, String> userConfig = new HashMap<>();
        userConfig.put("cluster.name", this.clusterName);
        userConfig.put(ElasticsearchSink.CONFIG_KEY_BULK_FLUSH_MAX_SIZE_MB, String.valueOf(this.bufferSize));
        List<InetSocketAddress> transports = new ArrayList<>();
        addTransportAddress(transports, this.address);
        return new ElasticsearchSink<>(userConfig, transports,
            new ElasticsearchTableFunction(this.index, this.jsonRowSchema, this.filedIndex, this.formatDate));
    }

    private void addTransportAddress(List<InetSocketAddress> transports, String serverList) {
        for (String server : serverList.split(",")) {
            try {
                String[] array = server.split(":");
                String host = array[0];
                int port = Integer.valueOf(array[1]);
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
