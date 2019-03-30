package com.dfire.platform.alchemy.api.function.scalar;

import com.dfire.platform.alchemy.api.function.BaseFunction;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 *  把某个字段根据指定的分隔符拆分成多个值，以list格式返回
 *  select split(field_name, '/')
 */
public class DateFormatFunction extends ScalarFunction implements BaseFunction{

    private static final String FUNCTION_NANME = "DATEFORMAT";

    private static final String FORMAT = "yyyy-MM-dd,HH:mm:ss.SSS";
    private static final Logger logger = LoggerFactory.getLogger(DateFormatFunction.class);

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }

    public String eval(String input, String srcFormat) {
        if (StringUtils.isBlank(input) || StringUtils.isBlank(srcFormat)){
            return input;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                srcFormat);
        Date date= null;
        try {
            date = simpleDateFormat.parse(input);
            SimpleDateFormat sdf=new SimpleDateFormat(FORMAT);
            return sdf.format(date);
        } catch (ParseException e) {
            logger.error("date format fail",e);
        }
        return input;
    }
}
