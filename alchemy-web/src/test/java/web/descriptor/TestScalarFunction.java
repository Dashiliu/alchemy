package web.descriptor;

import com.dfire.platform.alchemy.api.function.table.GeoIpFunction;
import com.dfire.platform.alchemy.api.function.table.UserAgentFunction;
import org.apache.commons.lang3.StringUtils;

import com.dfire.platform.alchemy.api.function.StreamScalarFunction;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author congbai
 * @date 07/06/2018
 */
public class TestScalarFunction implements StreamScalarFunction<String> {
    @Override
    public String invoke(Object... args) {
        return StringUtils.join(args);
    }

    @Test
    public void geoIp()throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
        tableEnv.registerFunction("geoIp", new GeoIpFunction());
        tableEnv.registerFunction("userAgent",new UserAgentFunction());

        List<Row> data = new ArrayList<>();
        data.add(Row.of(1, 1L, "a/b/c", "100"));
        data.add(Row.of(2, 2L, "Hello/admin", "200"));
        data.add(Row.of(3, 2L, "Hello/world", "300"));

        TypeInformation<?>[] types = {
            BasicTypeInfo.INT_TYPE_INFO,
            BasicTypeInfo.LONG_TYPE_INFO,
            BasicTypeInfo.STRING_TYPE_INFO,
            BasicTypeInfo.STRING_TYPE_INFO};
        String[] names = {"a", "b", "c", "d"};

        RowTypeInfo typeInfo = new RowTypeInfo(types, names);
        DataStream<Row> ds = env.fromCollection(data).returns(typeInfo);

        tableEnv.registerDataStream("Table1", ds, "a, b, c, d");

        Table table1 = tableEnv.sqlQuery("SELECT " +
            "geoIp('59.33.170.185') FROM Table1 AS t1");

        tableEnv.toAppendStream(table1, Row.class).print();
        env.execute();
    }

    @After
    public void after(){

    }
}
