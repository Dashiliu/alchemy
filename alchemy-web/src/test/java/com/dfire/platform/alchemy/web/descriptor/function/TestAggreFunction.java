package com.dfire.platform.alchemy.web.descriptor.function;

import java.util.ArrayList;
import java.util.List;

import com.dfire.platform.alchemy.function.StreamAggregateFunction;

/**
 * @author congbai
 * @date 06/06/2018
 */
public class TestAggreFunction implements StreamAggregateFunction<String, List, Integer> {

    @Override
    public List createAccumulator() {
        return new ArrayList();
    }

    @Override
    public void accumulate(List accumulator, String value) {
        accumulator.add(value);
    }

    @Override
    public Integer getValue(List accumulator) {
        return accumulator.size();
    }
}
