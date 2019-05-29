package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.api.util.GroovyCompiler;
import com.dfire.platform.alchemy.function.StreamAggregateFunction;
import com.dfire.platform.alchemy.function.StreamScalarFunction;
import com.dfire.platform.alchemy.function.StreamTableFunction;
import com.dfire.platform.alchemy.function.aggregate.FlinkAllAggregateFunction;
import com.dfire.platform.alchemy.function.scalar.FlinkAllScalarFunction;
import com.dfire.platform.alchemy.function.table.FlinkAllTableFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.web.common.ClusterType;

/**
 * 描述用户自定义函数
 *
 * @author congbai
 * @date 01/06/2018
 */
public class UdfDescriptor implements CoreDescriptor {

    private String name;

    /**
     * 如果是code读取方式，value就是代码内容 ；如果是jar包方式，则是className
     */
    private String value;

    private String type;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        if (ClusterType.FLINK.equals(clusterType)) {
            return transformFlink();
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);
    }

    @Override
    public String type() {
        return this.type;
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(name, "函数名不能为空");
    }

    private <T> T transformFlink() {
        try {
            Class clazz = GroovyCompiler.compile(this.getValue(), this.getName());
            Object udf = clazz.newInstance();
            if (udf instanceof StreamScalarFunction) {
                StreamScalarFunction<?> streamScalarFunction = (StreamScalarFunction<?>)udf;
                FlinkAllScalarFunction scalarFunction = new FlinkAllScalarFunction(streamScalarFunction);
                initScalarFuntion(streamScalarFunction, scalarFunction);
                return (T) scalarFunction;
            } else if (udf instanceof StreamTableFunction<?>) {
                StreamTableFunction<?> streamTableFunction = (StreamTableFunction)udf;
                FlinkAllTableFunction tableFunction = new FlinkAllTableFunction(streamTableFunction);
                initTableFunction(streamTableFunction, tableFunction);
                return (T) tableFunction;
            } else if (udf instanceof StreamAggregateFunction<?, ?, ?>) {
                StreamAggregateFunction<?, ?, ?> streamAggregateFunction = (StreamAggregateFunction)udf;
                FlinkAllAggregateFunction aggregateFunction
                    = new FlinkAllAggregateFunction((StreamAggregateFunction)udf);
                initAggregateFuntion(streamAggregateFunction, aggregateFunction);
                return (T) aggregateFunction;
            }else{
                return (T) udf;
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid UDF " + this.getName(), ex);
        }
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
