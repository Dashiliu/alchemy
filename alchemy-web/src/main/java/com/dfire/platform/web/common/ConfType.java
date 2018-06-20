package com.dfire.platform.web.common;

/**
 * @author congbai
 * @date 2018/6/8
 */
public enum ConfType {

    JAR(0), SOURCE(1), UDF(2), SINK(3), SQL(4)

    ;

    private int type;

    ConfType(int type) {
        this.type = type;
    }

    public static ConfType fromType(int type) {
        for (ConfType b : values()) {
            if (b != null && b.getType() == type) {
                return b;
            }
        }
        return null;
    }

    public int getType() {
        return type;
    }
}
