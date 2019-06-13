package com.dfire.platform.alchemy.formats.grok;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author congbai
 * @date 2018/8/7
 */
public class GrokRowDeserializationSchema implements DeserializationSchema<Row> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(GrokRowDeserializationSchema.class);

    private final TypeInformation<Row> typeInfo;

    private final String regular;

    private final boolean retain;

    private final String fieldName;

    public GrokRowDeserializationSchema(TypeInformation<Row> typeInfo, String regular, boolean retain, String fieldName) {
        this.typeInfo = typeInfo;
        this.regular = regular;
        this.retain = retain;
        this.fieldName = fieldName;
        if(this.retain && this.fieldName ==null){
            throw new IllegalArgumentException("grok fieldName must be not null");
        }
    }

    public GrokRowDeserializationSchema(TypeInformation<Row> typeInfo, String regular) {
       this(typeInfo, regular , false, null);
    }

    @Override
    public Row deserialize(byte[] bytes) throws IOException {
        String message = new String(bytes,"utf-8");
        return convertToRow(message, ((RowTypeInfo)typeInfo).getFieldNames(),regular);
    }

    public Row convertToRow(String message, String[] names, String regular) {
        final Row row = new Row(names.length);
        Map<String, Object> grokMap = GrokProxy.getInstance().match(message, regular);
        for (int i = 0; i < names.length; i++) {
            try {
                final String name = names[i];
                if (retain && fieldName.equals(name)){
                    row.setField(i, message);
                    continue;
                }
                Object field = grokMap.get(name);
                row.setField(i, field);
            } catch (Exception e) {
                logger.error("Occur Error when grok convert to Row",e);
            }
        }
        return row;
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
