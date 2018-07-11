package com.dfire.platform.alchemy.api.util;

import org.apache.flink.types.Row;

/**
 * @author congbai
 * @date 2018/7/10
 */
public class RowUtils {

    public static Object[] createRows(Row value) {
        Object[] rows = new Object[value.getArity()];
        for (int i = 0; i < value.getArity(); i++) {
            rows[i] = value.getField(i);
        }
        return rows;
    }

}
