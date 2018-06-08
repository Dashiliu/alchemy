package web.descriptor;

import com.dfire.platform.api.function.StreamTableFunction;

/**
 * @author congbai
 * @date 06/06/2018
 */
public class TestTableFunction extends StreamTableFunction<String> {

    @Override
    public void invoke(Object... args) {
        for (Object arg : args) {
            collect(String.valueOf(arg));
        }
    }
}
