package com.dfire.platform.alchemy.connectors.rocketmq;

import java.util.List;
import java.util.Objects;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.Types;
import org.apache.flink.table.api.ValidationException;
import org.apache.flink.table.sources.DefinedProctimeAttribute;
import org.apache.flink.table.sources.DefinedRowtimeAttributes;
import org.apache.flink.table.sources.RowtimeAttributeDescriptor;
import org.apache.flink.table.sources.StreamTableSource;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;

import com.dfire.platform.alchemy.connectors.rocketmq.serialization.KeyedDeserializationSchemaWrapper;

import scala.Option;

/**
 * @author congbai
 * @date 2018/9/14
 */
public class RocketMQTableSource implements StreamTableSource<Row>, DefinedProctimeAttribute, DefinedRowtimeAttributes {

    private final DeserializationSchema<Row> deserializationSchema;

    private final TableSchema schema;

    private final RocketMQConsumerProperties properties;

    private List<RowtimeAttributeDescriptor> rowtimeAttributeDescriptors;

    private TypeInformation<Row> returnType;

    private String proctimeAttribute;

    public RocketMQTableSource(DeserializationSchema<Row> deserializationSchema, TypeInformation<Row> returnType,
        TableSchema schema, RocketMQConsumerProperties properties) {
        this.deserializationSchema = deserializationSchema;
        this.schema = Preconditions.checkNotNull(schema, "Schema must not be null.");;
        this.properties = properties;
        this.returnType = Preconditions.checkNotNull(returnType, "Type information must not be null.");
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
        return "RocketMQTableSource";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RocketMQTableSource)) {
            return false;
        }
        RocketMQTableSource that = (RocketMQTableSource)o;
        return Objects.equals(schema, that.schema) && Objects.equals(properties, that.properties)
            && Objects.equals(returnType, that.returnType) && Objects.equals(proctimeAttribute, that.proctimeAttribute)
            && Objects.equals(rowtimeAttributeDescriptors, that.rowtimeAttributeDescriptors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, properties, returnType, proctimeAttribute, rowtimeAttributeDescriptors);
    }

    @Override
    public String getProctimeAttribute() {
        return this.proctimeAttribute;
    }

    /**
     * Declares a field of the schema to be the processing time attribute.
     *
     * @param proctimeAttribute The name of the field that becomes the processing time field.
     */
    public void setProctimeAttribute(String proctimeAttribute) {
        if (proctimeAttribute != null) {
            // validate that field exists and is of correct type
            Option<TypeInformation<?>> tpe = schema.getType(proctimeAttribute);
            if (tpe.isEmpty()) {
                throw new ValidationException(
                    "Processing time attribute '" + proctimeAttribute + "' is not present in TableSchema.");
            } else if (tpe.get() != Types.SQL_TIMESTAMP()) {
                throw new ValidationException(
                    "Processing time attribute '" + proctimeAttribute + "' is not of type SQL_TIMESTAMP.");
            }
        }
        this.proctimeAttribute = proctimeAttribute;
    }

    @Override
    public List<RowtimeAttributeDescriptor> getRowtimeAttributeDescriptors() {
        return this.rowtimeAttributeDescriptors;
    }

    /**
     * Declares a list of fields to be rowtime attributes.
     *
     * @param rowtimeAttributeDescriptors The descriptors of the rowtime attributes.
     */
    public void setRowtimeAttributeDescriptors(List<RowtimeAttributeDescriptor> rowtimeAttributeDescriptors) {
        // validate that all declared fields exist and are of correct type
        for (RowtimeAttributeDescriptor desc : rowtimeAttributeDescriptors) {
            String rowtimeAttribute = desc.getAttributeName();
            Option<TypeInformation<?>> tpe = schema.getType(rowtimeAttribute);
            if (tpe.isEmpty()) {
                throw new ValidationException(
                    "Rowtime attribute '" + rowtimeAttribute + "' is not present in TableSchema.");
            } else if (tpe.get() != Types.SQL_TIMESTAMP()) {
                throw new ValidationException(
                    "Rowtime attribute '" + rowtimeAttribute + "' is not of type SQL_TIMESTAMP.");
            }
        }
        this.rowtimeAttributeDescriptors = rowtimeAttributeDescriptors;
    }

    public DeserializationSchema<Row> getDeserializationSchema() {
        return deserializationSchema;
    }

    public TableSchema getSchema() {
        return schema;
    }

    @Override
    public DataStream<Row> getDataStream(StreamExecutionEnvironment execEnv) {
        DeserializationSchema<Row> deserializationSchema = getDeserializationSchema();
        RocketMQConsumer<Row> kafkaConsumer
            = new RocketMQConsumer(new KeyedDeserializationSchemaWrapper(deserializationSchema), properties);
        return execEnv.addSource(kafkaConsumer).name(explainSource());
    }
}
