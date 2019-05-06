package com.dfire.platform.alchemy.connectors.rocketmq.serialization;

import org.apache.flink.api.common.serialization.SerializationSchema;

/**
 * @author congbai
 * @date 2018/9/14
 */
public class KeyedSerializationSchemaWrapper<T> implements KeyValueSerializationSchema<T> {

    private static final long serialVersionUID = 1;

    private final SerializationSchema<T> serializationSchema;

    public KeyedSerializationSchemaWrapper(SerializationSchema<T> serializationSchema) {
        this.serializationSchema = serializationSchema;
    }

    @Override
    public byte[] serializeKey(T tuple) {
        return null;
    }

    @Override
    public byte[] serializeValue(T tuple) {
        return serializationSchema.serialize(tuple);
    }
}
