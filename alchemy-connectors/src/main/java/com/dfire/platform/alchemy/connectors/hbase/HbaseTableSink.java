package com.dfire.platform.alchemy.connectors.hbase;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.sink.OutputFormatSinkFunction;
import org.apache.flink.streaming.util.serialization.JsonRowSerializationSchema;
import org.apache.flink.table.sinks.TableSink;
import org.apache.flink.table.sinks.UpsertStreamTableSink;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;

import com.dfire.platform.alchemy.api.sink.HbaseInvoker;
import com.dfire.platform.alchemy.api.util.GroovyCompiler;
import com.dfire.platform.alchemy.api.util.RandomUtils;

/**
 * @author congbai
 * @date 05/06/2018
 */
public class HbaseTableSink implements UpsertStreamTableSink<Row> {

    private final String zookeeper;

    private final String node;

    private final String tableName;

    private final String family;

    private final long bufferSize;

    private final String code;

    private HbaseInvoker hbaseInvoker;

    private SerializationSchema<Row> serializationSchema;

    private String[] fieldNames;

    private TypeInformation[] fieldTypes;

    public HbaseTableSink(String zookeeper, String node, String tableName, String family, long bufferSize,
        String code) {
        this.zookeeper = zookeeper;
        this.node = node;
        this.tableName = tableName;
        this.family = family;
        this.bufferSize = bufferSize;
        this.code = Preconditions.checkNotNull(code, "code can not be null ");
    }

    public HbaseTableSink(String zookeeper, String node, String tableName, String family, long bufferSize,
        HbaseInvoker hbaseInvoker) {
        this.zookeeper = zookeeper;
        this.node = node;
        this.tableName = tableName;
        this.family = family;
        this.bufferSize = bufferSize;
        this.code = null;
        this.hbaseInvoker = Preconditions.checkNotNull(hbaseInvoker, "hbaseInvoker can not be null ");
    }

    @Override
    public String[] getFieldNames() {
        return this.fieldNames;
    }

    @Override
    public TypeInformation<?>[] getFieldTypes() {
        return this.fieldTypes;
    }

    @Override
    public TableSink<Tuple2<Boolean, Row>> configure(String[] fieldNames, TypeInformation<?>[] fieldTypes) {
        HbaseTableSink copy;
        if (hbaseInvoker == null) {
            copy = new HbaseTableSink(this.zookeeper, this.node, this.tableName, this.family, this.bufferSize,
                this.code);
        } else {
            copy = new HbaseTableSink(this.zookeeper, this.node, this.tableName, this.family, this.bufferSize,
                this.hbaseInvoker);
        }
        copy.fieldNames = Preconditions.checkNotNull(fieldNames, "fieldNames");
        copy.fieldTypes = Preconditions.checkNotNull(fieldTypes, "fieldTypes");
        Preconditions.checkArgument(fieldNames.length == fieldTypes.length,
            "Number of provided field names and types does not match.");

        RowTypeInfo rowSchema = new RowTypeInfo(fieldTypes, fieldNames);
        copy.serializationSchema = new JsonRowSerializationSchema(rowSchema);
        if (this.hbaseInvoker == null) {
            copy.hbaseInvoker = createInvoker(this.code);
        }
        return copy;
    }

    private HbaseInvoker createInvoker(String code) {
        Class clazz = GroovyCompiler.compile(code, RandomUtils.uuid());
        try {
            return (HbaseInvoker)clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setKeyFields(String[] keys) {

    }

    @Override
    public void setIsAppendOnly(Boolean isAppendOnly) {

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
        OutputFormatSinkFunction hbaseSink = creatHbaseSink();
        dataStream.addSink(hbaseSink);
    }

    private OutputFormatSinkFunction creatHbaseSink() {
        return new OutputFormatSinkFunction(new HBaseOutputFormat(this.zookeeper, this.node, this.tableName,
            this.family, this.bufferSize, this.fieldNames.length, this.serializationSchema, this.hbaseInvoker));
    }

}
