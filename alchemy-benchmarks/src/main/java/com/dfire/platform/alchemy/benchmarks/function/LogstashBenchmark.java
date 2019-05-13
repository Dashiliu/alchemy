package com.dfire.platform.alchemy.benchmarks.function;


import com.dfire.platform.alchemy.function.logstash.util.useragent.UserAgentUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author congbai
 * @date 03/05/2018
 */
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class LogstashBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(LogstashBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(5)
                .build();
        new Runner(opt).run();
    }


    @Benchmark
    public void useragent() {
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/7.0.4(0x17000428) NetType/4G Language/zh_CN";
        UserAgentUtil.parse(userAgent);
    }

}
