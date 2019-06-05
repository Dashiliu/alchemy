package com.dfire.platform.alchemy.common;

/**
 * @author congbai
 * @date 2018/6/30
 */
public class Timestamps {

    private String type;
    private String from;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public static enum Type {

        FIELD("from-field");

        private String type;

        Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
