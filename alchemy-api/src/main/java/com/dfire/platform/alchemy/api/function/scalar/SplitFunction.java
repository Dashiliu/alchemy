package com.dfire.platform.alchemy.api.function.scalar;

import com.dfire.platform.alchemy.api.function.BaseFunction;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 *  把某个字段根据指定的分隔符拆分成多个值，以list格式返回
 *  select split(field_name, '/')
 */
public class SplitFunction extends ScalarFunction implements BaseFunction{

    private static final String FUNCTION_NANME = "SPLIT";
    private static final Logger logger = LoggerFactory.getLogger(SplitFunction.class);

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }

    public List<String> eval(String input, String separator) {
        if (StringUtils.isBlank(input)) {
            return Collections.emptyList();
        }
        String[] array = input.split(separator);
        return Arrays.asList(array);
    }


}
