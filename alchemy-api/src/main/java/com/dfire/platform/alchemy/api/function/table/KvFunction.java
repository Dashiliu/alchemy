package com.dfire.platform.alchemy.api.function.table;

import com.dfire.platform.alchemy.api.function.BaseFunction;
import com.dfire.platform.alchemy.api.logstash.Mutate;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.functions.TableFunction;
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
public class KvFunction extends TableFunction<Map<String,Object>> implements BaseFunction, Serializable {

    private static final String FUNCTION_NANME = "KV";
    private static final Logger logger = LoggerFactory.getLogger(KvFunction.class);

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }

    public void eval(String input, String separator, String prefix) {
        Map<String, Object> map = Mutate.kv(input,separator,prefix);
        collect(map);
//        return map;
    }

//    @Override
//    public TypeInformation<Map<String, String>> getResultType() {
//        //return MapTypeInfo.of(HashMap.class);
//        return new MapTypeInfo(TypeInformation.of(String.class),TypeInformation.of(String.class));
//    }
}
