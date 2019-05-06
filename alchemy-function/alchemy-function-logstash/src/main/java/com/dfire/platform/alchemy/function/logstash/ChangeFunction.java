package com.dfire.platform.alchemy.function.logstash;


import com.dfire.platform.alchemy.function.BaseFunction;
import org.apache.flink.table.functions.ScalarFunction;

/***
 *
 * change    修改类型
   select change(field_name, 'int') or select change(field_name, 'integer')
   select change(field_name, 'long')
   select change(field_name, 'short')
   select change(field_name, 'string')
 */
public class ChangeFunction extends ScalarFunction implements BaseFunction {

    private static final String FUNCTION_NANME = "change";

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }

    public Object eval(String input, String type) {
        if ("integer".equalsIgnoreCase(type) || "int".equalsIgnoreCase(type)) {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        if ("long".equalsIgnoreCase(type)) {
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        if ("short".equalsIgnoreCase(type)) {
            try {
                return Short.parseShort(input);
            } catch (NumberFormatException e) {
                return (short)0;
            }
        }
        if ("string".equalsIgnoreCase(type)) {
            return String.valueOf(input);
        }
        return input;
    }


}
