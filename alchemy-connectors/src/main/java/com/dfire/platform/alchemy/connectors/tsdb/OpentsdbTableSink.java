package com.dfire.platform.alchemy.connectors.tsdb;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.table.sinks.AppendStreamTableSink;
import org.apache.flink.table.sinks.TableSink;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;

import com.dfire.platform.alchemy.api.sink.OpentsdbInvoker;

/**
 * @author congbai
 * @date 2018/7/10
 */
public class OpentsdbTableSink implements AppendStreamTableSink<Row> {

    private final String opentsdbUrl;

    private final Integer ioThreadCount;

    private final Integer batchPutBufferSize;

    private final Integer batchPutConsumerThreadCount;

    private final Integer batchPutSize;

    private final Integer batchPutTimeLimit;

    private final Integer putRequestLimit;

    private final String code;

    private OpentsdbInvoker invoker;

    private String[] fieldNames;

    private TypeInformation[] fieldTypes;

    public OpentsdbTableSink(String opentsdbUrl, Integer ioThreadCount, Integer batchPutBufferSize, Integer batchPutConsumerThreadCount, Integer batchPutSize, Integer batchPutTimeLimit, Integer putRequestLimit, String code) {
        this.opentsdbUrl = opentsdbUrl;
        this.ioThreadCount = ioThreadCount;
        this.batchPutBufferSize = batchPutBufferSize;
        this.batchPutConsumerThreadCount = batchPutConsumerThreadCount;
        this.batchPutSize = batchPutSize;
        this.batchPutTimeLimit = batchPutTimeLimit;
        this.putRequestLimit = putRequestLimit;
        this.code = code;
    }

    public OpentsdbTableSink(String opentsdbUrl, Integer ioThreadCount, Integer batchPutBufferSize, Integer batchPutConsumerThreadCount, Integer batchPutSize, Integer batchPutTimeLimit, Integer putRequestLimit, OpentsdbInvoker invoker) {
        this.opentsdbUrl = opentsdbUrl;
        this.ioThreadCount = ioThreadCount;
        this.batchPutBufferSize = batchPutBufferSize;
        this.batchPutConsumerThreadCount = batchPutConsumerThreadCount;
        this.batchPutSize = batchPutSize;
        this.batchPutTimeLimit = batchPutTimeLimit;
        this.putRequestLimit = putRequestLimit;
        this.code = null;
        this.invoker=invoker;
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
    public TypeInformation<Row> getOutputType() {
        return new RowTypeInfo(this.fieldTypes);
    }

    @Override
    public TableSink<Row> configure(String[] fieldNames, TypeInformation<?>[] fieldTypes) {
        OpentsdbTableSink copy;
        if (invoker == null) {
            copy = new OpentsdbTableSink(this.opentsdbUrl,this.ioThreadCount,this.batchPutBufferSize,this.batchPutConsumerThreadCount,
                    this.batchPutSize,this.batchPutTimeLimit,this.putRequestLimit,
                    this.code);
        } else {
            copy = new OpentsdbTableSink(this.opentsdbUrl,this.ioThreadCount,this.batchPutBufferSize,this.batchPutConsumerThreadCount,
                    this.batchPutSize,this.batchPutTimeLimit,this.putRequestLimit,
                    this.invoker);
        }
        copy.fieldNames = Preconditions.checkNotNull(fieldNames, "fieldNames");
        copy.fieldTypes = Preconditions.checkNotNull(fieldTypes, "fieldTypes");
        Preconditions.checkArgument(fieldNames.length == fieldTypes.length,
                "Number of provided field names and types does not match.");
        return copy;
    }

    @Override
    public void emitDataStream(DataStream<Row> dataStream) {
        RichSinkFunction richSinkFunction = createTsdbRich();
        dataStream.addSink(richSinkFunction);
    }

    private RichSinkFunction createTsdbRich() {
        if (invoker == null) {
             return new OpentsdbSinkFunction(this.opentsdbUrl,this.ioThreadCount,this.batchPutBufferSize,this.batchPutConsumerThreadCount,
                    this.batchPutSize,this.batchPutTimeLimit,this.putRequestLimit,
                    this.code);
        } else {
            return new OpentsdbSinkFunction(this.opentsdbUrl,this.ioThreadCount,this.batchPutBufferSize,this.batchPutConsumerThreadCount,
                    this.batchPutSize,this.batchPutTimeLimit,this.putRequestLimit,
                    this.invoker);
        }

    }
}
