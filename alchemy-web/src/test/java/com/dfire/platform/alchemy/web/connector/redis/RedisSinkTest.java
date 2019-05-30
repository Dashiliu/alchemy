package com.dfire.platform.alchemy.web.connector.redis;

import com.dfire.platform.alchemy.web.cluster.flink.SqlSubmitFlinkRequest;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.util.BindPropertiesUtils;
import org.junit.Test;
import org.springframework.util.ResourceUtils;
import com.dfire.platform.alchemy.web.BaseCluster;

import java.io.File;
import java.util.Map;

/**
 * @author congbai
 * @date 2019/5/28
 */
public class RedisSinkTest extends BaseCluster {

    @Test
    public void simpleWrite() throws Exception {
        Response response = execute("select * from csv_table_test","set");
        assert response.isSuccess();
    }

    @Test
    public void groupByWrite() throws Exception {
        Response response = execute("select c.first,sum(c.score) from csv_table_test  c  group by c.first ","set");
        assert response.isSuccess();
    }

    @Test
    public void hset() throws Exception {
        Response response = execute("select * from csv_table_test","hset");
        assert response.isSuccess();
    }

    @Test
    public void rpush() throws Exception {
        Response response = execute("select * from csv_table_test","rpush");
        assert response.isSuccess();
    }

    @Test
    public void sadd() throws Exception {
        Response response = execute("select * from csv_table_test","sadd");
        assert response.isSuccess();
    }

    @Test
    public void zadd() throws Exception {
        Response response = execute("select * from csv_table_test","zadd");
        assert response.isSuccess();
    }

    Response execute(String sql,String command) throws Exception {
        File file = ResourceUtils.getFile("classpath:redis/redis.yaml");
        SqlSubmitFlinkRequest sqlSubmitFlinkRequest
            = BindPropertiesUtils.bindProperties(file, SqlSubmitFlinkRequest.class);
        sqlSubmitFlinkRequest.setTest(true);
        sqlSubmitFlinkRequest.getTable().setSql(sql);
        sqlSubmitFlinkRequest.setJobName("test_mysql_side");
        Map<String,Object> redisSink = sqlSubmitFlinkRequest.getTable().getSinks().get(0);
        redisSink.put("command", command);
        return this.cluster.send(sqlSubmitFlinkRequest);
    }
}
