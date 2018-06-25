package com.dfire.platform.alchemy.web.common;

/**
 * @author congbai
 * @date 07/06/2018
 */
public class TimeAttribute {

    private String timeCharacteristic;

    private String attribute;

    private boolean exist;

    private long maxOutOfOrderness;

    public String getTimeCharacteristic() {
        return timeCharacteristic;
    }

    public void setTimeCharacteristic(String timeCharacteristic) {
        this.timeCharacteristic = timeCharacteristic;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public long getMaxOutOfOrderness() {
        return maxOutOfOrderness;
    }

    public void setMaxOutOfOrderness(long maxOutOfOrderness) {
        this.maxOutOfOrderness = maxOutOfOrderness;
    }
}
