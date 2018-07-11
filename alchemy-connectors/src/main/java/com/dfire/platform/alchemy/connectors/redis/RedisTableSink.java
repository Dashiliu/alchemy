package com.dfire.platform.alchemy.connectors.redis;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.table.sinks.TableSink;
import org.apache.flink.table.sinks.UpsertStreamTableSink;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;

import com.dfire.platform.alchemy.api.sink.RedisInvoker;

/**
 * @author congbai
 * @date 07/06/2018
 */
public class RedisTableSink implements UpsertStreamTableSink<Row> {

    private final String sentinels;

    private final String master;

    private final int database;

    private final Integer maxTotal;

    private final Integer queueSize;

    private final Integer threadNum;

    private final String code;

    private final RedisInvoker redisInvoker;

    private String[] fieldNames;

    private TypeInformation[] fieldTypes;

    public RedisTableSink(String sentinels, String master, int database, Integer maxTotal, Integer queueSize, Integer threadNum, String code) {
        this.sentinels = sentinels;
        this.master = master;
        this.database = database;
        this.maxTotal = maxTotal;
        this.queueSize = queueSize;
        this.threadNum = threadNum;
        this.code = code;
        this.redisInvoker=null;
    }

    public RedisTableSink(String sentinels, String master, int database, Integer maxTotal, Integer queueSize, Integer threadNum, RedisInvoker redisInvoker) {
        this.sentinels = sentinels;
        this.master = master;
        this.database = database;
        this.maxTotal = maxTotal;
        this.queueSize = queueSize;
        this.threadNum = threadNum;
        this.code=null;
        this.redisInvoker = redisInvoker;
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
        RedisTableSink copy;
        if (redisInvoker == null) {
            copy = new RedisTableSink(this.sentinels,this.master,this.database,this.maxTotal,this.queueSize,this.threadNum,
                    this.code);
        } else {
            copy = new RedisTableSink(this.sentinels,this.master,this.database,this.maxTotal,this.queueSize,this.threadNum,
                    this.redisInvoker);
        }
        copy.fieldNames = Preconditions.checkNotNull(fieldNames, "fieldNames");
        copy.fieldTypes = Preconditions.checkNotNull(fieldTypes, "fieldTypes");
        Preconditions.checkArgument(fieldNames.length == fieldTypes.length,
                "Number of provided field names and types does not match.");
        return copy;
    }

    @Override
    public void setKeyFields(String[] keys) {

    }

    @Override
    public void setIsAppendOnly(Boolean isAppendOnly) {

    }

    @Override
    public void emitDataStream(DataStream<Tuple2<Boolean, Row>> dataStream) {
        RichSinkFunction richSinkFunction = creatRedisRich();
        dataStream.addSink(richSinkFunction);
    }

    private RichSinkFunction creatRedisRich() {
        if (this.redisInvoker != null) {
            return new RedisSinkFunction(this.sentinels,this.master,this.database,this.maxTotal,this.queueSize,this.threadNum,
                    this.redisInvoker);
        } else {
            return new RedisSinkFunction(this.sentinels,this.master,this.database,this.maxTotal,this.queueSize,this.threadNum,
                    this.code);
        }
    }

    @Override
    public TupleTypeInfo<Tuple2<Boolean, Row>> getOutputType() {
        return new TupleTypeInfo(Types.BOOLEAN, getRecordType());
    }

    @Override
    public TypeInformation<Row> getRecordType() {
        return new RowTypeInfo(getFieldTypes());
    }

}
