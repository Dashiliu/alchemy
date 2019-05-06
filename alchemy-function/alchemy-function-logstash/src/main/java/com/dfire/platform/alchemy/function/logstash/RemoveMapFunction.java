package com.dfire.platform.alchemy.function.logstash;

import com.dfire.platform.alchemy.function.BaseFunction;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

/***
 * 根据分隔符转换成数组，并为每个元素统一加上前缀prefix
 * select kv(field_name, '&', 'prefix_')
 */
public class RemoveMapFunction extends ScalarFunction implements BaseFunction, Serializable {

    private static final String FUNCTION_NANME = "REMOVE";
    private static final Logger logger = LoggerFactory.getLogger(RemoveMapFunction.class);

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }


    public  Map<String,Object> eval(Map<String, Object> input, String... fields) {
        if (input == null || fields == null) {
            return input;
        }

        for (String field:fields){
            input.remove(field);
        }

        return input;
    }
}
