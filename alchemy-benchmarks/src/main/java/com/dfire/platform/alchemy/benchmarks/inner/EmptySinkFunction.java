package com.dfire.platform.alchemy.benchmarks.inner;

import java.util.concurrent.atomic.LongAdder;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author congbai
 * @date 2018/7/10
 */
public class EmptySinkFunction extends RichSinkFunction<Row> {

    private static final Logger LOG = LoggerFactory.getLogger(EmptySinkFunction.class);

    private LongAdder count = new LongAdder();

    public EmptySinkFunction() {

    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }

    @Override
    public void close() throws Exception {
        super.close();
    }

    @Override
    public void invoke(Row value, Context context) throws Exception {
        count.increment();
    }
}
