package com.dfire.platform.alchemy.api.function.table;

import com.dfire.platform.alchemy.api.function.BaseFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.typeutils.MapTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/***
 * 根据分隔符转换成数组，并为每个元素统一加上前缀prefix
 * select kv(field_name, '&', 'prefix_')
 */
public class KvFunction extends TableFunction<Map<String,String>> implements BaseFunction,Serializable{

    private static final String FUNCTION_NANME = "kv";
    private static final Logger logger = LoggerFactory.getLogger(KvFunction.class);

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }

    public void eval(String input, String separator, String prefix) {
        if (StringUtils.isBlank(input)) {
            return;
        }
        String[] array = input.split(separator);
        HashMap<String,String> map = new HashMap<>(array.length);
        for (String single : array) {
            String[] singleArray = single.split("=");
            if (singleArray.length>1){
                map.put(prefix +singleArray[0],singleArray[1]);
            }
        }

        collect(map);
    }

    @Override
    public TypeInformation<Map<String, String>> getResultType() {
        //return MapTypeInfo.of(HashMap.class);
        return new MapTypeInfo(TypeInformation.of(String.class),TypeInformation.of(String.class));
    }
}
