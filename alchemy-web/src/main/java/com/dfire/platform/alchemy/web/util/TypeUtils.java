package com.dfire.platform.alchemy.web.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

/**
 * @author congbai
 * @date 2018/7/1
 */
public class TypeUtils {

    public static TypeInformation readTypeInfo(String type) {
        if (StringUtils.isEmpty(type)) {
            return null;
        }
        Keyword keyword = Keyword.fromKeyword(type);
        if (keyword == null) {
            return null;
        }
        switch (keyword) {
            case VARCHAR:
                return Types.STRING;
            case BOOLEAN:
                return Types.BOOLEAN;
            case TINYINT:
                return Types.BYTE;
            case SMALLINT:
                return Types.SHORT;
            case INT:
                return Types.INT;
            case BIGINT:
                return Types.LONG;
            case FLOAT:
                return Types.FLOAT;
            case DOUBLE:
                return Types.DOUBLE;
            case DECIMAL:
                return Types.DECIMAL;
            case DATE:
                return Types.SQL_DATE;
            case TIME:
                return Types.SQL_TIME;
            case TIMESTAMP:
                return Types.SQL_TIMESTAMP;
            default:
                break;
        }
        return null;
    }

    enum Keyword {
        VARCHAR("VARCHAR"), BOOLEAN("BOOLEAN"), TINYINT("TINYINT"), SMALLINT("SMALLINT"), INT("INT"), BIGINT("BIGINT"),
        FLOAT("FLOAT"), DOUBLE("DOUBLE"), DECIMAL("DECIMAL"), DATE("DATE"), TIME("TIME"), TIMESTAMP("TIMESTAMP"),
        ROW("ROW"), ANY("ANY"), POJO("POJO"),;
        private String keyword;

        Keyword(String keyword) {
            this.keyword = keyword;
        }

        public static Keyword fromKeyword(String type) {
            for (Keyword b : values()) {
                if (b != null && b.getKeyword().equals(type)) {
                    return b;
                }
            }
            return null;
        }

        public String getKeyword() {
            return keyword;
        }
    }

}
