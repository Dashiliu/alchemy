package com.dfire.platform.web.descriptor;

import java.lang.reflect.Type;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.dfire.platform.api.function.StreamAggregateFunction;
import com.dfire.platform.api.function.StreamScalarFunction;
import com.dfire.platform.api.function.StreamTableFunction;
import com.dfire.platform.api.function.aggregate.FlinkAllAggregateFunction;
import com.dfire.platform.api.function.scalar.FlinkAllScalarFunction;
import com.dfire.platform.api.function.table.FlinkAllTableFunction;
import com.dfire.platform.api.util.GroovyCompiler;
import com.dfire.platform.web.common.ClusterType;
import com.dfire.platform.web.common.ReadMode;

/**
 * 描述用户自定义函数
 * 
 * @author congbai
 * @date 01/06/2018
 */
@Component
public class UdfDescriptor implements Descriptor {

    private int readMode = ReadMode.CODE.getMode();

    private String name;

    /**
     * 如果是code读取方式，value就是代码内容 ；如果是jar包方式，则是className
     */
    private String value;

    public static void main(String[] args) {

        Class clazz = FlinkAllScalarFunction.class;
        Type type = clazz.getGenericSuperclass();
    }

    public int getReadMode() {
        return readMode;
    }

    public void setReadMode(int readMode) {
        this.readMode = readMode;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getContentType() {
        return "udf";
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        if (ClusterType.FLINK.equals(clusterType)) {
            return transformFlink();
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(name, "函数名不能为空");
        Assert.notNull(value, "函数体不能为空");
        Class clazz = GroovyCompiler.compile(value, name);
        clazz.newInstance();
    }

    private <T> T transformFlink() {
        if (ReadMode.JAR.equals(this.readMode)) {
            try {
                Class clazz = Class.forName(this.value);
                Object udf = clazz.newInstance();
                if (udf instanceof StreamScalarFunction) {
                    StreamScalarFunction<?> streamScalarFunction = (StreamScalarFunction<?>)udf;
                    FlinkAllScalarFunction scalarFunction = new FlinkAllScalarFunction(streamScalarFunction);
                    initScalarFuntion(streamScalarFunction, scalarFunction);
                    return (T)scalarFunction;
                } else if (udf instanceof StreamTableFunction<?>) {
                    StreamTableFunction<?> streamTableFunction = (StreamTableFunction)udf;
                    FlinkAllTableFunction tableFunction = new FlinkAllTableFunction(streamTableFunction);
                    initTableFunction(streamTableFunction, tableFunction);
                    return (T)tableFunction;
                } else if (udf instanceof StreamAggregateFunction<?, ?, ?>) {
                    StreamAggregateFunction<?, ?, ?> streamAggregateFunction = (StreamAggregateFunction)udf;
                    FlinkAllAggregateFunction aggregateFunction
                        = new FlinkAllAggregateFunction((StreamAggregateFunction)udf);
                    initAggregateFuntion(streamAggregateFunction, aggregateFunction);
                    return (T)aggregateFunction;
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid UDF " + this.name, ex);
            }
        } else if (ReadMode.CODE.equals(this.readMode)) {
            try {
                Class<StreamScalarFunction> clazz = GroovyCompiler.compile(this.value, this.name);
                Object udf = clazz.newInstance();
                if (udf instanceof StreamScalarFunction) {
                    StreamScalarFunction streamScalarFunction = (StreamScalarFunction)udf;
                    FlinkAllScalarFunction scalarFunction = new FlinkAllScalarFunction(this.value, this.name);
                    initScalarFuntion(streamScalarFunction, scalarFunction);
                    return (T)scalarFunction;
                } else if (udf instanceof StreamTableFunction<?>) {
                    StreamTableFunction<?> streamTableFunction = (StreamTableFunction)udf;
                    FlinkAllTableFunction tableFunction = new FlinkAllTableFunction(this.value, this.name);
                    initTableFunction(streamTableFunction, tableFunction);
                    return (T)tableFunction;
                } else if (udf instanceof StreamAggregateFunction<?, ?, ?>) {
                    StreamAggregateFunction<?, ?, ?> streamAggregateFunction = (StreamAggregateFunction)udf;
                    FlinkAllAggregateFunction aggregateFunction = new FlinkAllAggregateFunction(this.value, this.name);
                    initAggregateFuntion(streamAggregateFunction, aggregateFunction);
                    return (T)aggregateFunction;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid UDF " + this.name, e);
            }

        }
        throw new IllegalArgumentException("Invalid UDF readMode" + this.readMode);
    }

    private void initScalarFuntion(StreamScalarFunction<?> streamScalarFunction,
        FlinkAllScalarFunction flinkAllScalarFunction) {
        flinkAllScalarFunction.setResultType(TypeExtractor.createTypeInfo(streamScalarFunction,
            StreamScalarFunction.class, streamScalarFunction.getClass(), 0));
    }

    private void initAggregateFuntion(StreamAggregateFunction<?, ?, ?> streamAggregateFunction,
        FlinkAllAggregateFunction aggregateFunction) {
        aggregateFunction.setProducedType(TypeExtractor.createTypeInfo(streamAggregateFunction,
            StreamAggregateFunction.class, streamAggregateFunction.getClass(), 0));
        aggregateFunction.setAccumulatorType(TypeExtractor.createTypeInfo(streamAggregateFunction,
            StreamAggregateFunction.class, streamAggregateFunction.getClass(), 1));
        aggregateFunction.setResultType(TypeExtractor.createTypeInfo(streamAggregateFunction,
            StreamAggregateFunction.class, streamAggregateFunction.getClass(), 2));
        aggregateFunction.setAccumulator(streamAggregateFunction.createAccumulator());
    }

    private void initTableFunction(StreamTableFunction<?> streamTableFunction, FlinkAllTableFunction tableFunction) {
        TypeInformation typeInformation = TypeExtractor.createTypeInfo(streamTableFunction, StreamTableFunction.class,
            streamTableFunction.getClass(), 0);
        tableFunction.setProducedType(typeInformation);
        tableFunction.setResultType(typeInformation);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
