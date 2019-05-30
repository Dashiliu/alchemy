package com.dfire.platform.alchemy.api.util;

import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author congbai
 * @date 2018/8/7
 */
public class ConvertRowUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConvertRowUtils.class);

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
            } catch (Exception e) {
                logger.error("Occur Error when convert from Row",e);
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
            } catch (Exception e) {
                logger.error("Occur Error when convert to Row",e);
            }
        }
        return row;
    }

    public static Row grokConvertToRow(String message, String[] names, String regular) {
        final Row row = new Row(names.length);
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
                logger.error("Occur Error when grok convert to Row",e);
            }
        }
        return row;
    }
}
