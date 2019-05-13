package com.dfire.platform.alchemy.benchmarks.inner;

import com.dfire.platform.alchemy.benchmarks.Metric;
import com.dfire.platform.alchemy.benchmarks.generate.GenerateLog;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

public class LogSource<T> extends RichParallelSourceFunction<T> implements ResultTypeQueryable<T> {

    private volatile boolean running = true;

    private long maxValue;

    private final DeserializationSchema<T> deserializationSchema;

    private final GenerateLog generateLog;

    public LogSource(long maxValue, DeserializationSchema<T> deserializationSchema, GenerateLog generateLog) {
        this.maxValue = maxValue;
        this.deserializationSchema = deserializationSchema;
        this.generateLog = generateLog;
    }

    @Override
    public void run(SourceContext<T> ctx) throws Exception {
        long counter = 0;
        while (running) {
            synchronized (ctx.getCheckpointLock()) {
                long beginTime = System.nanoTime()/1000L;
                ctx.collect(deserializationSchema.deserialize(generateLog.generate()));
                counter++;
                if (counter >= maxValue) {
                    cancel();
                }
                long end = System.nanoTime()/1000;
                long responseTime = end - beginTime;
                if (responseTime > Metric.maxTime) {
                    Metric.maxTime = responseTime;
                }
                if (responseTime < Metric.minTime || Metric.minTime == -1) {
                    Metric.minTime = responseTime;
                }
                Metric.sumResponseTimeSpread(responseTime);
            }
        }
    }

    @Override
    public void cancel() {
        running = false;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return deserializationSchema.getProducedType();
    }
}
