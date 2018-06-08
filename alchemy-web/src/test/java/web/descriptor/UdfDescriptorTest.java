package web.descriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.flink.table.functions.FunctionContext;
import org.junit.Test;

import com.dfire.platform.api.function.aggregate.FlinkAllAggregateFunction;
import com.dfire.platform.api.function.scalar.FlinkAllScalarFunction;
import com.dfire.platform.web.common.ClusterType;
import com.dfire.platform.web.common.ReadMode;
import com.dfire.platform.web.descriptor.UdfDescriptor;

/**
 * @author congbai
 * @date 06/06/2018
 */
public class UdfDescriptorTest {

    @Test
    public void streamScalar()
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        TestScalarFunction testScalarFunction = new TestScalarFunction();
        Class clazz = FlinkAllScalarFunction.class;
        clazz.asSubclass(testScalarFunction.getClass().getInterfaces()[0]);
        Constructor c = clazz.getConstructor(testScalarFunction.getClass().getInterfaces()[0]);
        FlinkAllScalarFunction flinkAllScalarFunction = (FlinkAllScalarFunction)c.newInstance(testScalarFunction);

    }

    @Test
    public void transformCode() throws Exception {
        UdfDescriptor udfDescriptor = new UdfDescriptor();
        udfDescriptor.setReadMode(ReadMode.CODE);
        udfDescriptor.setName("transformCode");
        udfDescriptor.setValue("import com.dfire.platform.api.function.StreamScalarFunction;\n" + "\n" + "/**\n"
            + " * @author congbai\n" + " * @date 06/06/2018\n" + " */\n"
            + "public class TestFunction implements StreamScalarFunction<Integer> {\n" + "\n" + "    @Override\n"
            + "    public Integer invoke(Object... args) {\n" + "        Integer result=2222;\n"
            + "        return result;\n" + "    }\n" + "}\n");
        FlinkAllScalarFunction udf = udfDescriptor.transform(ClusterType.FLINK);
        udf.open(null);
        Object value = udf.eval("test");
        assert value.equals(2222);
    }

    @Test
    public void transformClass() throws Exception {
        UdfDescriptor udfDescriptor = new UdfDescriptor();
        udfDescriptor.setReadMode(ReadMode.JAR);
        udfDescriptor.setName("transformCode");
        udfDescriptor.setValue("web.descriptor.TestFunction");
        FlinkAllAggregateFunction udf = udfDescriptor.transform(ClusterType.FLINK);
        udf.open(new FunctionContext(null));
        Object acc = udf.createAccumulator();
        udf.accumulate(acc, "111");
        udf.accumulate(acc, "222");
        Integer value = (Integer)udf.getValue(acc);
        assert value == 2;
    }
}
