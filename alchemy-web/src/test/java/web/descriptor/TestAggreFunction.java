package web.descriptor;

import java.util.ArrayList;
import java.util.List;

import com.dfire.platform.api.function.StreamAggregateFunction;

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
