package com.dfire.platform.alchemy.web.common;

/**
 * @author congbai
 * @date 2019/5/20
 */
public class Alias {

    private String table;

    private String alias;

    public Alias(String table, String alias) {
        this.table = table;
        this.alias = alias;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
