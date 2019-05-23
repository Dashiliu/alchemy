package com.dfire.platform.alchemy.web.side;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

/**
 * @author congbai
 * @date 2019/5/21
 */
public abstract class SyncReqRow  extends RichFlatMapFunction<Row, Row> implements ISideReqRow{


    @Override
    public void flatMap(Row value, Collector<Row> out) throws Exception {

    }
}
