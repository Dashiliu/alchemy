package com.dfire.platform.alchemy.benchmarks.inner;

import com.dfire.platform.alchemy.benchmarks.generate.GenerateLog;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.sources.StreamTableSource;
import org.apache.flink.types.Row;

/**
 * @author congbai
 * @date 2019/4/22
 */
public class LogTableSource implements StreamTableSource<Row> {

    private final DeserializationSchema<Row> deserializationSchema;

    private final TableSchema schema;

    private final TypeInformation<Row> returnType;

    private final GenerateLog generateLog;

    private final Long maxValue;

    public LogTableSource(DeserializationSchema<Row> deserializationSchema, TableSchema schema, TypeInformation<Row> returnType, GenerateLog generateLog, Long maxValue) {
        this.deserializationSchema = deserializationSchema;
        this.schema = schema;
        this.returnType = returnType;
        this.generateLog = generateLog;
        this.maxValue = maxValue;
    }

    @Override
    public DataStream<Row> getDataStream(StreamExecutionEnvironment execEnv) {
        LogSource<Row> kafkaConsumer
                = new LogSource(maxValue,deserializationSchema, generateLog);
        return execEnv.addSource(kafkaConsumer).name(explainSource());
    }

    @Override
    public TypeInformation<Row> getReturnType() {
        return this.returnType;
    }

    @Override
    public TableSchema getTableSchema() {
        return this.schema;
    }

    @Override
    public String explainSource() {
        return "Log";
    }
}
