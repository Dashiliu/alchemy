package com.dfire.platform.alchemy.function.logstash;

import com.dfire.platform.alchemy.function.BaseFunction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuntun on 2019/5/14 0014.
 */
public class TimestampFunction extends ScalarFunction implements BaseFunction {

    private static final Logger logger = LoggerFactory.getLogger(DateFormatFunction.class);

    private static final String FUNCTION_NANME = "TIMESTAMP";

    private static final String FORMAT = "yyyy-MM-dd,HH:mm:ss.SSS";

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMA = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(FORMAT);
        }
    };

    private Map<String,FastDateFormat> dateFormats = new HashMap<>();

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }

    public String eval(String input, String srcFormat) {
        if (StringUtils.isBlank(input) || StringUtils.isBlank(srcFormat)){
            return input;
        }

        FastDateFormat fastDateFormat = dateFormats.get(srcFormat);
        if (fastDateFormat == null) {
            fastDateFormat = FastDateFormat.getInstance(srcFormat);
            dateFormats.put(srcFormat , fastDateFormat);
        }
        try {
            Date date = fastDateFormat.parse(input);
            return DATE_FORMA.get().format(date);
        } catch (ParseException e) {
            logger.error("date format fail",e);
        }
        return input;
    }
}
