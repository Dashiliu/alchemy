package com.dfire.platform.alchemy.formats.grok;

import com.dfire.platform.alchemy.api.util.ConvertRowUtils;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;

import java.io.IOException;

/**
 * @author congbai
 * @date 2018/8/7
 */
public class GrokRowDeserializationSchema implements DeserializationSchema<Row> {

    private final TypeInformation<Row> typeInfo;

    private final String regular;

    public GrokRowDeserializationSchema(TypeInformation<Row> typeInfo, String regular) {
        this.typeInfo = typeInfo;
        this.regular = regular;
    }

    @Override
    public Row deserialize(byte[] bytes) throws IOException {
        String message = new String(bytes,"utf-8");
        return ConvertRowUtils.grokConvertToRow(message, ((RowTypeInfo)typeInfo).getFieldNames(),regular);
    }

    @Override
    public boolean isEndOfStream(Row nextElement) {
        return false;
    }

    @Override
    public TypeInformation<Row> getProducedType() {
        return this.typeInfo;
    }
}
