package com.dfire.platform.alchemy.web.side;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.types.Row;

import static org.apache.flink.util.Preconditions.checkArgument;

/**
 * @author congbai
 * @date 2019/5/21
 */
public abstract class AsyncReqRow<T>  extends RichAsyncFunction<Row, Row> implements ISideReqRow<T> {

    private final SideTableInfo sideTable;

    private Cache<T> cache;

    protected AsyncReqRow(SideTableInfo sideTable) {
        checkArgument(sideTable != null && sideTable.getSide() != null , "side can't be null");
        this.sideTable = sideTable;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        this.cache = create(sideTable.getSide());
    }

    public SideTableInfo getSideTable() {
        return sideTable;
    }

    public Cache<T> getCache() {
        return cache;
    }
}
