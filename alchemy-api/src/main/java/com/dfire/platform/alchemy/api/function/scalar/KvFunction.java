package com.dfire.platform.alchemy.api.function.scalar;

import com.dfire.platform.alchemy.api.function.BaseFunction;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/***
 * 根据分隔符转换成数组，并为每个元素统一加上前缀prefix
 * select kv(field_name, '&', 'prefix_')
 */
public class KvFunction extends ScalarFunction implements BaseFunction, Serializable {

    private static final String FUNCTION_NANME = "KV";
    private static final Logger logger = LoggerFactory.getLogger(KvFunction.class);

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }

    public Map<String, String> eval(String input, String separator, String prefix) {
        if (StringUtils.isBlank(input)) {
            return Collections.emptyMap();
        }
        String[] array = input.split(separator);
        Map<String, String> map = new HashMap<>(array.length);
        for (String single : array) {
            String[] singleArray = single.split("=");
            if (singleArray.length > 1) {
                map.put(prefix + singleArray[0], singleArray[1]);
            }
        }
        return map;
    }

//    @Override
//    public TypeInformation<Map<String, String>> getResultType() {
//        //return MapTypeInfo.of(HashMap.class);
//        return new MapTypeInfo(TypeInformation.of(String.class),TypeInformation.of(String.class));
//    }
}
