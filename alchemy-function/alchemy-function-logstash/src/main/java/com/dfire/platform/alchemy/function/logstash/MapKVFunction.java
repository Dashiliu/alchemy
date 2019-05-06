package com.dfire.platform.alchemy.function.logstash;

import com.dfire.platform.alchemy.api.util.Mutate;
import com.dfire.platform.alchemy.function.BaseFunction;
import org.apache.flink.table.functions.ScalarFunction;

import java.io.Serializable;
import java.util.Map;

/***
 * 根据分隔符转换成数组，并为每个元素统一加上前缀prefix
 * select kv(field_name, '&', 'prefix_')
 */
public class MapKVFunction extends ScalarFunction implements BaseFunction, Serializable {

    private static final String FUNCTION_NANME = "MAPKVCHANGE";

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }


    public Map<String,Object> eval(Map<String, Object> input, String field, String method, String... methodArgs) {
        if (input == null) {
            return null;
        }
        Object obj = input.get(field);
        if (obj == null){
            return null;
        }
        if ("KV".equals(method)){
            return Mutate.kv((String)obj,methodArgs[0],methodArgs[1]);
        }
        return null;
    }
}
