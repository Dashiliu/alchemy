package com.dfire.platform.alchemy.api.function.scalar;

import com.dfire.platform.alchemy.api.function.BaseFunction;
import com.dfire.platform.alchemy.api.logstash.GrokProxy;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

/***
 * 根据分隔符转换成数组，并为每个元素统一加上前缀prefix
 * select kv(field_name, '&', 'prefix_')
 */
public class GrokFunction extends ScalarFunction implements BaseFunction, Serializable {

    private static final String FUNCTION_NANME = "GROK";

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }

    public static Map<String, Object> eval(String message, String pattern)  {
        if (StringUtils.isBlank(message)){
            return null;
        }
        return GrokProxy.getInstance().match(message,pattern);
    }

}
