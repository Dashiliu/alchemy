package com.dfire.platform.alchemy.connectors.elasticsearch;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.elasticsearch5.ElasticsearchSink;
import org.apache.flink.table.sinks.TableSink;
import org.apache.flink.table.sinks.UpsertStreamTableSink;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;

/**
 * @author congbai
 * @date 05/06/2018
 */
public class ElasticsearchTableSink implements UpsertStreamTableSink<Row> {

    private final String address;

    private final String clusterName;

    private final String index;

    private final int bufferSize;

    private String[] fieldNames;

    private TypeInformation[] fieldTypes;

    private JsonRowStringSchema jsonRowSchema;

    public ElasticsearchTableSink(String address, String clusterName, String index, int bufferSize) {
        this.address = address;
        this.clusterName = clusterName;
        this.index = index;
        this.bufferSize = bufferSize;
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
    public TableSink<Tuple2<Boolean, Row>> configure(String[] fieldNames, TypeInformation<?>[] fieldTypes) {
        ElasticsearchTableSink copy
            = new ElasticsearchTableSink(this.address, this.clusterName, this.index, this.bufferSize);
        copy.fieldNames = Preconditions.checkNotNull(fieldNames, "fieldNames");
        copy.fieldTypes = Preconditions.checkNotNull(fieldTypes, "fieldTypes");
        Preconditions.checkArgument(fieldNames.length == fieldTypes.length,
            "Number of provided field names and types does not match.");

        RowTypeInfo rowSchema = new RowTypeInfo(fieldTypes, fieldNames);
        copy.jsonRowSchema = new JsonRowStringSchema(rowSchema);

        return copy;
    }

    @Override
    public void setKeyFields(String[] keys) {

    }

    @Override
    public void setIsAppendOnly(Boolean isAppendOnly) {
        System.out.println(isAppendOnly);
    }

    @Override
    public TupleTypeInfo<Tuple2<Boolean, Row>> getOutputType() {
        return new TupleTypeInfo(Types.BOOLEAN, getRecordType());
    }

    @Override
    public TypeInformation<Row> getRecordType() {
        return new RowTypeInfo(getFieldTypes());
    }

    @Override
    public void emitDataStream(DataStream<Tuple2<Boolean, Row>> dataStream) {
        ElasticsearchSink<Tuple2<Boolean, Row>> elasticsearchSink = createEsSink();
        dataStream.addSink(elasticsearchSink);

    }

    private ElasticsearchSink<Tuple2<Boolean, Row>> createEsSink() {
        Map<String, String> userConfig = new HashMap<>();
        userConfig.put("cluster.name", this.clusterName);
        userConfig.put(ElasticsearchSink.CONFIG_KEY_BULK_FLUSH_MAX_SIZE_MB, String.valueOf(this.bufferSize));
        List<InetSocketAddress> transports = new ArrayList<>();
        addTransportAddress(transports, this.address);
        return new ElasticsearchSink<>(userConfig, transports,
            new ElasticsearchTableFunction(this.index, this.jsonRowSchema));
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
}
