package com.dfire.platform.alchemy.benchmarks;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.*;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author congbai
 * @date 2018/11/19
 */
public class ProcessProfiler implements InternalProfiler {

    private static final List<String> OPERATING_SYSTEM_BEAN_CLASS_NAMES = Arrays.asList(
            "com.sun.management.OperatingSystemMXBean", // HotSpot
            "com.ibm.lang.management.OperatingSystemMXBean" // J9
    );

    private final OperatingSystemMXBean operatingSystemBean;


    private final Class<?> operatingSystemBeanClass;


    private final Method systemCpuUsage;


    private final Method processCpuUsage;

    public ProcessProfiler() {
        this.operatingSystemBean = ManagementFactory.getOperatingSystemMXBean();
        this.operatingSystemBeanClass = getFirstClassFound(OPERATING_SYSTEM_BEAN_CLASS_NAMES);
        this.systemCpuUsage = detectMethod("getSystemCpuLoad");
        this.processCpuUsage = detectMethod("getProcessCpuLoad");
    }

    @Override
    public String getDescription() {
        return "process";
    }


    private double invoke(Method method) {
        try {
            return method != null ? (double) method.invoke(operatingSystemBean) : Double.NaN;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return Double.NaN;
        }
    }


    private Method detectMethod(String name) {
        requireNonNull(name);
        if (operatingSystemBeanClass == null) {
            return null;
        }
        try {
            // ensure the Bean we have is actually an instance of the interface
            operatingSystemBeanClass.cast(operatingSystemBean);
            return operatingSystemBeanClass.getDeclaredMethod(name);
        } catch (ClassCastException | NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    private Class<?> getFirstClassFound(List<String> classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ignore) {
            }
        }
        return null;
    }

    @Override
    public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {

    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams, IterationResult result) {
        List<ScalarResult> results = new ArrayList<ScalarResult>();
        results.add(
                new ScalarResult(
                        Defaults.PREFIX + "system.load.average.1m",
                        operatingSystemBean.getSystemLoadAverage(),
                        "",
                        AggregationPolicy.AVG));
        results.add(
                new ScalarResult(
                        Defaults.PREFIX + "system.cpu.usage",
                        invoke(systemCpuUsage),
                        "",
                        AggregationPolicy.AVG));
        results.add(
                new ScalarResult(
                        Defaults.PREFIX + "process.cpu.usage",
                        invoke(processCpuUsage),
                        "",
                        AggregationPolicy.AVG));
        return results;
    }
}
