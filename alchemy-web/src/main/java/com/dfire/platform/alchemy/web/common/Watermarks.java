package com.dfire.platform.alchemy.web.common;

/**
 * @author congbai
 * @date 2018/6/30
 */
public class Watermarks {

    private String type;
    private long delay;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public static enum Type {

        PERIODIC_BOUNDED("periodic-bounded");

        private String type;

        Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
