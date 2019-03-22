package com.dfire.platform.alchemy.api.function.scalar;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.ScalarFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/***
 * 根据分隔符转换成数组，并为每个元素统一加上前缀prefix
 * select kv(field_name, '&', 'prefix_')
 */
public class KvFunction extends ScalarFunction {

    public List<String> eval(String input, String separator, String prefix) {
        if (StringUtils.isBlank(input)) {
            return Collections.emptyList();
        }
        String[] array = input.split(separator);
        List<String> list = new ArrayList<>(array.length);
        for (String single : array) {
            list.add(prefix + single);
        }
        return list;
    }
}
