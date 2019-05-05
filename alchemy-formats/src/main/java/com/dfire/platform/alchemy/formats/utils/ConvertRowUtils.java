package com.dfire.platform.alchemy.formats.utils;

import com.dfire.platform.alchemy.api.logstash.GrokProxy;
import org.apache.flink.types.Row;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author congbai
 * @date 2018/8/7
 */
public class ConvertRowUtils {

    public static void convertFromRow(Object object, String[] fieldNames, Row row) {
        Class clazz = object.getClass();
        // validate the row
        if (row.getArity() != fieldNames.length) {
            throw new IllegalStateException(
                    String.format("Number of elements in the row '%s' is different from number of field names: %d", row,
                            fieldNames.length));
        }

        for (int i = 0; i < fieldNames.length; i++) {
            if (row.getField(i) == null) {
                continue;
            }
            final String name = fieldNames[i];
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(object, row.getField(i));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    public static Row convertToRow(Object obj, String[] names) {
        Class clazz = obj.getClass();
        final Row row = new Row(names.length);
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            try {
                Field field = clazz.getDeclaredField(name);
                if (field == null) {
                    row.setField(i, null);
                    continue;
                }
                field.setAccessible(true);
                row.setField(i, field.get(obj));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return row;
    }

    public static Row grokConvertToRow(String message, String[] names, String regular) {
        final Row row = new Row(names.length);
//        names[names.length+1] = "client_ip";
        Map<String, Object> grokMap = GrokProxy.getInstance().match(message, regular);
        for (int i = 0; i < names.length; i++) {
            try {
                final String name = names[i];
                if ("message".equals(name)){
                    row.setField(i, message);
                    continue;
                }
                Object field = grokMap.get(name);
                row.setField(i, field);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        row.setField(names.length,"39.187.114.9");
        return row;
    }
}
