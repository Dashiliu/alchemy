package com.dfire.platform.alchemy.formats.protostuff;

import java.io.IOException;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;

import com.dfire.platform.alchemy.formats.utils.ConvertRowUtils;

import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * @author congbai
 * @date 2018/8/7
 */
public class ProtostuffRowDeserializationSchema implements DeserializationSchema<Row> {

    private final TypeInformation<Row> typeInfo;

    private transient final Schema schema;

    public ProtostuffRowDeserializationSchema(TypeInformation<Row> typeInfo,Class clazz) {
        this.schema = RuntimeSchema.getSchema(clazz);
        this.typeInfo = typeInfo;
    }

    @Override
    public Row deserialize(byte[] bytes) throws IOException {
        try {
            Object obj = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
            // convert to row
            return ConvertRowUtils.convertToRow(obj, ((RowTypeInfo)typeInfo).getFieldNames());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isEndOfStream(Row row) {
        return false;
    }

    @Override
    public TypeInformation<Row> getProducedType() {
        return this.typeInfo;
    }
}
