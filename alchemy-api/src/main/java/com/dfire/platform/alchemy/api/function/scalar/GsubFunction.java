package com.dfire.platform.alchemy.api.function.scalar;

import com.dfire.platform.alchemy.api.function.BaseFunction;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *
 * gsub    替换
 * select gsub(field_name)
 */
public class GsubFunction extends ScalarFunction implements BaseFunction{

    private static final String FUNCTION_NANME = "GSUB";
    private static final Logger logger = LoggerFactory.getLogger(GsubFunction.class);

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }


    public String eval(String input, String regex, String replacement) {
        if (StringUtils.isNotBlank(input)) {
            return input.replaceAll(regex, replacement);
        }
        return input;
    }
}
