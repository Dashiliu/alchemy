package com.dfire.platform.alchemy.web.common;

/**
 * @author congbai
 * @date 2018/6/8
 */
public enum ConfType {

    JAR(0), CONFIG(1), SQL(2)

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
