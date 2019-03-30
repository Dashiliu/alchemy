package com.dfire.platform.alchemy.formats.grok;

import com.caucho.hessian.io.HessianInput;
import com.dfire.platform.alchemy.formats.utils.ConvertRowUtils;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;

import java.io.ByteArrayInputStream;
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
        String message = new String(bytes);
//        String message = "[global-proxy003] [ngxacc:] \"100.116.234.76\" \"-\" \"[26/Mar/2019:16:51:46 +0800]\" \"GET /d?url=server.2dfire.com HTTP/1.1\" \"200\" \"96\" \"-\" \"Dalvik/1.6.0 (Linux; U; Android 4.4.2; rk3188 Build/KOT49H)\" \"GET -\" \"120.55.199.20\" \"39.187.114.9\" \"0.004\" \"47020\" \"0.004\" \"-\" \"/d\" \"200\" \"10.25.0.152:6789\" \"-\" \"3b570966015e31c807f9008b56852765\" \"http\"";
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
