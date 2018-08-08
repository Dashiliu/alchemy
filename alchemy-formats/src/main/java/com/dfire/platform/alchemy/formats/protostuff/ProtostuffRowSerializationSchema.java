package com.dfire.platform.alchemy.formats.protostuff;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;

import com.dfire.platform.alchemy.formats.utils.ConvertRowUtils;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * @author congbai
 * @date 2018/8/7
 */
public class ProtostuffRowSerializationSchema implements SerializationSchema<Row> {

    private final static ThreadLocal<LinkedBuffer> bufThreadLocal = new ThreadLocal<LinkedBuffer>() {

        @Override
        protected LinkedBuffer initialValue() {
            return LinkedBuffer.allocate();
        }
    };
    private final TypeInformation<Row> typeInfo;
    private transient final Schema schema;

    public ProtostuffRowSerializationSchema(Class clazz, TypeInformation<Row> typeInfo) {
        this.schema = RuntimeSchema.getSchema(clazz);
        this.typeInfo = typeInfo;
    }

    @Override
    public byte[] serialize(Row row) {
        LinkedBuffer buf = bufThreadLocal.get();
        try {
            Object object = schema.newMessage();
            ConvertRowUtils.convertFromRow(object, ((RowTypeInfo)typeInfo).getFieldNames(), row);
            return ProtostuffIOUtil.toByteArray(object, schema, buf);
        } catch (Throwable t) {
            throw new RuntimeException(
                "Could not serialize row '" + row + "'. " + "Make sure that the schema matches the input.", t);
        } finally {
            buf.clear();
        }
    }

}
