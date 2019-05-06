package com.dfire.platform.alchemy.connectors.rocketmq;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.formats.json.JsonRowSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.sinks.AppendStreamTableSink;
import org.apache.flink.table.sinks.TableSink;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;

import com.dfire.platform.alchemy.connectors.rocketmq.serialization.KeyedSerializationSchemaWrapper;

/**
 * @author congbai
 * @date 2018/9/14
 */
public class RocketMQTableSink implements AppendStreamTableSink<Row> {

    private final RocketMQProducerProperties properties;

    private SerializationSchema<Row> serializationSchema;

    private String[] fieldNames;

    private TypeInformation[] fieldTypes;

    public RocketMQTableSink(RocketMQProducerProperties properties) {
        this.properties = properties;
    }

    @Override
    public TypeInformation<Row> getOutputType() {
        return new RowTypeInfo(getFieldTypes());
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
    public TableSink<Row> configure(String[] fieldNames, TypeInformation<?>[] fieldTypes) {
        RocketMQTableSink copy = new RocketMQTableSink(this.properties);
        copy.fieldNames = Preconditions.checkNotNull(fieldNames, "fieldNames");
        copy.fieldTypes = Preconditions.checkNotNull(fieldTypes, "fieldTypes");
        Preconditions.checkArgument(fieldNames.length == fieldTypes.length,
            "Number of provided field names and types does not match.");

        RowTypeInfo rowSchema = new RowTypeInfo(fieldTypes, fieldNames);
        copy.serializationSchema = new JsonRowSerializationSchema(rowSchema);

        return copy;
    }

    @Override
    public void emitDataStream(DataStream<Row> dataStream) {
        RocketMQProducer<Row> rocketMQProducer
            = new RocketMQProducer<>(new KeyedSerializationSchemaWrapper(serializationSchema), this.properties);
        dataStream.addSink(rocketMQProducer).name("rocketmq:" + this.properties.getTopic());
    }
}
