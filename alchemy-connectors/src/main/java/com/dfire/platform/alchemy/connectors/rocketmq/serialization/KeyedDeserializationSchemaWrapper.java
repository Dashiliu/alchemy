package com.dfire.platform.alchemy.connectors.rocketmq.serialization;

import java.io.IOException;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

/**
 * @author congbai
 * @date 2018/9/14
 */
public class KeyedDeserializationSchemaWrapper<T> implements KeyValueDeserializationSchema<T> {

    private static final long serialVersionUID = 1;

    private final DeserializationSchema<T> deserializationSchema;

    public KeyedDeserializationSchemaWrapper(DeserializationSchema<T> deserializationSchema) {
        this.deserializationSchema = deserializationSchema;
    }

    @Override
    public T deserializeKeyAndValue(byte[] key, byte[] value) throws IOException {
        return deserializationSchema.deserialize(value);
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return deserializationSchema.getProducedType();
    }
}
