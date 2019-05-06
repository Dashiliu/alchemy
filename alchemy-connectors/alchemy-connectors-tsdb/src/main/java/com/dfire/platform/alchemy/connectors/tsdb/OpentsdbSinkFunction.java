package com.dfire.platform.alchemy.connectors.tsdb;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dfire.platform.alchemy.api.common.TsdbData;
import com.dfire.platform.alchemy.api.sink.OpentsdbInvoker;
import com.dfire.platform.alchemy.api.util.GroovyCompiler;
import com.dfire.platform.alchemy.api.util.RandomUtils;
import com.dfire.platform.alchemy.api.util.RowUtils;
import com.dfire.platform.alchemy.connectors.tsdb.handler.HitsdbHandler;
import com.dfire.platform.alchemy.connectors.tsdb.handler.OpentsdbHandler;
import com.dfire.platform.alchemy.connectors.tsdb.handler.TsdbHandler;

/**
 * @author congbai
 * @date 2018/7/10
 */
public class OpentsdbSinkFunction extends RichSinkFunction<Row> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(OpentsdbSinkFunction.class);

    private final OpentsdbProperties opentsdbProperties;

    private final String code;

    private transient OpentsdbInvoker invoker;

    private transient TsdbHandler tsdbHandler;

    public OpentsdbSinkFunction(OpentsdbProperties opentsdbProperties, String code) {
        this.opentsdbProperties = opentsdbProperties;
        this.code = code;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        if (StringUtils.isEmpty(this.opentsdbProperties.getEnv())
            || "publish".equals(this.opentsdbProperties.getEnv())) {
            this.tsdbHandler = new HitsdbHandler(this.opentsdbProperties);
        } else {
            this.tsdbHandler = new OpentsdbHandler(this.opentsdbProperties);
        }
        try {
            Class clazz = Class.forName(code);
            this.invoker = (OpentsdbInvoker)clazz.newInstance();
        } catch (Exception e) {
            this.invoker = GroovyCompiler.create(this.code, RandomUtils.uuid());
        }
        super.open(parameters);
    }

    @Override
    public void close() throws Exception {
        this.tsdbHandler.close();
        super.close();
    }

    @Override
    public void invoke(Row value, Context context) throws Exception {
        if (value == null) {
            return;
        }
        List<TsdbData> tsdbDatas = this.invoker.create(RowUtils.createRows(value));
        if (tsdbDatas == null || tsdbDatas.isEmpty()) {
            return;
        }
        for (TsdbData tsdbData : tsdbDatas) {
            if (tsdbData.getTimestamp() == null) {
                tsdbData.setTimestamp(context.currentWatermark() > 0 ? context.currentWatermark() / 1000
                    : System.currentTimeMillis() / 1000);
            }
            this.tsdbHandler.execute(tsdbData);
        }
    }
}
