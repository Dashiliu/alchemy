package com.dfire.platform.alchemy.connectors.tsdb;

import java.util.List;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.hitsdb.client.HiTSDB;
import com.aliyun.hitsdb.client.HiTSDBClientFactory;
import com.aliyun.hitsdb.client.HiTSDBConfig;
import com.aliyun.hitsdb.client.callback.BatchPutCallback;
import com.aliyun.hitsdb.client.value.Result;
import com.aliyun.hitsdb.client.value.request.Point;
import com.dfire.platform.alchemy.api.common.TsdbData;
import com.dfire.platform.alchemy.api.sink.OpentsdbInvoker;
import com.dfire.platform.alchemy.api.util.GroovyCompiler;
import com.dfire.platform.alchemy.api.util.RandomUtils;
import com.dfire.platform.alchemy.api.util.RowUtils;

/**
 * @author congbai
 * @date 2018/7/10
 */
public class OpentsdbSinkFunction extends RichSinkFunction<Row> {

    private static final Logger LOG = LoggerFactory.getLogger(OpentsdbSinkFunction.class);

    private final String opentsdbUrl;

    private final Integer ioThreadCount;

    private final Integer batchPutBufferSize;

    private final Integer batchPutConsumerThreadCount;

    private final Integer batchPutSize;

    private final Integer batchPutTimeLimit;

    private final Integer putRequestLimit;

    private final String code;

    private OpentsdbInvoker invoker;


    private transient HiTSDB tsdb;

    public OpentsdbSinkFunction(String opentsdbUrl,
                                Integer ioThreadCount,
                                Integer batchPutBufferSize,
                                Integer batchPutConsumerThreadCount,
                                Integer batchPutSize,
                                Integer batchPutTimeLimit,
                                Integer putRequestLimit,
                                String code) {
        this.opentsdbUrl = opentsdbUrl;
        this.ioThreadCount = ioThreadCount;
        this.batchPutBufferSize = batchPutBufferSize;
        this.batchPutConsumerThreadCount = batchPutConsumerThreadCount;
        this.batchPutSize = batchPutSize;
        this.batchPutTimeLimit = batchPutTimeLimit;
        this.putRequestLimit = putRequestLimit;
        this.code=code;
        this.invoker=null;
    }

    public OpentsdbSinkFunction(String opentsdbUrl,
                                Integer ioThreadCount,
                                Integer batchPutBufferSize,
                                Integer batchPutConsumerThreadCount,
                                Integer batchPutSize,
                                Integer batchPutTimeLimit,
                                Integer putRequestLimit,
                                OpentsdbInvoker invoker) {
        this.opentsdbUrl = opentsdbUrl;
        this.ioThreadCount = ioThreadCount;
        this.batchPutBufferSize = batchPutBufferSize;
        this.batchPutConsumerThreadCount = batchPutConsumerThreadCount;
        this.batchPutSize = batchPutSize;
        this.batchPutTimeLimit = batchPutTimeLimit;
        this.putRequestLimit = putRequestLimit;
        this.code=null;
        this.invoker=invoker;
    }


    @Override
    public void open(Configuration parameters) throws Exception {
        HiTSDBConfig.Builder builder = HiTSDBConfig
                // 配置地址，第一个参数可以是域名，IP。
                .address(opentsdbUrl, 8242)
                // 异步写相关，异步批量 Put 回调接口。
                .listenBatchPut(new BatchPutCallback() {
                    @Override
                    public void response(String s, List<Point> list, Result result) {
                        //nothing to  do
                        LOG.info("处理成功:{},result:{}", list.size(), result.toJSON());
                    }

                    @Override
                    public void failed(String address, List<Point> input, Exception ex) {
                        ex.printStackTrace();
                        LOG.error("失败处理:{}", input.size(), ex);
                    }
                })
                // 流量限制。设置每秒最大提交Point的个数。
                .maxTPS(50000);
        if(this.ioThreadCount!=null){
            builder.ioThreadCount(this.ioThreadCount);
        }
        if(this.batchPutBufferSize!=null){
            builder.batchPutBufferSize(this.batchPutBufferSize);
        }
        if(this.batchPutConsumerThreadCount!=null){
            builder.batchPutConsumerThreadCount(this.batchPutConsumerThreadCount);
        }
        if(this.batchPutSize!=null){
            builder.batchPutSize(this.batchPutSize);
        }
        if(this.batchPutTimeLimit!=null){
            builder.batchPutTimeLimit(this.batchPutTimeLimit);
        }
        if(this.putRequestLimit!=null){
            builder.putRequestLimit(this.putRequestLimit);
        }
        this.tsdb = HiTSDBClientFactory.connect(builder.config());
        if (this.invoker == null) {
            this.invoker = GroovyCompiler.create(this.code, RandomUtils.uuid());
        }
        super.open(parameters);
    }

    @Override
    public void close() throws Exception {
        this.tsdb.close();
        super.close();
    }

    @Override
    public void invoke(Row value, Context context) throws Exception {
        if(value==null){
            return;
        }
        List<TsdbData> tsdbDatas=this.invoker.create(RowUtils.createRows(value));
        if(tsdbDatas==null||tsdbDatas.isEmpty()){
            return;
        }
        for(TsdbData tsdbData:tsdbDatas){
            if(tsdbData.getMetricValues()==null
                    ||tsdbData.getMetricValues().isEmpty()
                    ||tsdbData.getTags()==null
                    ||tsdbData.getTags().isEmpty()){
                continue;
            }
            long timestamp = context.currentWatermark()>0?context.currentWatermark()/ 1000:System.currentTimeMillis()/1000;
            tsdbData.getMetricValues().entrySet().forEach((entry) -> {
                Point point = Point
                        .metric(entry.getKey())
                        .timestamp(timestamp)
                        .tag(tsdbData.getTags())
                        .value(entry.getValue())
                        .build();
                this.tsdb.put(point);
            });
        }
    }
}
