package com.dfire.platform.alchemy.api.function.scalar;

import com.dfire.platform.alchemy.api.function.BaseFunction;
import com.dfire.platform.alchemy.api.logstash.Mutate;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

/***
 * 根据分隔符转换成数组，并为每个元素统一加上前缀prefix
 * select kv(field_name, '&', 'prefix_')
 */
public class MapFunction extends ScalarFunction implements BaseFunction, Serializable {

    private static final String FUNCTION_NANME = "MAPCHANGE";
    private static final Logger logger = LoggerFactory.getLogger(MapFunction.class);

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }


    public  Object eval(Map<String, Object> input, String field, String method, String... methodArgs) {
        if (input == null) {
            return null;
        }
        Object obj = input.get(field);
        if (obj == null){
            return obj;
        }
        if ("CHANGE".equals(method)) {
            if (methodArgs == null || methodArgs.length == 0) {
                return null;
            }
            input.remove(field);
            obj = Mutate.change(obj, methodArgs[0]);
        } else if ("URLDECODE".equals(method)) {
            input.remove(field);
            obj = Mutate.urldecode(obj);
        }else if ("KV".equals(method)){
            obj = Mutate.kv((String)obj,methodArgs[0],methodArgs[1]);
        }
        return obj;
    }
}
