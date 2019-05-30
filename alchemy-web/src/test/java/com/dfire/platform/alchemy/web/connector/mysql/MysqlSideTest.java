package com.dfire.platform.alchemy.web.connector.mysql;

import java.io.File;

import org.junit.Test;
import org.springframework.util.ResourceUtils;

import com.dfire.platform.alchemy.web.cluster.flink.SqlSubmitFlinkRequest;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.util.BindPropertiesUtils;

import com.dfire.platform.alchemy.web.BaseCluster;

/**
 * @author congbai
 * @date 2019/5/24
 */
public class MysqlSideTest extends BaseCluster {

    @Test
    public void simple_mysql() throws Exception {
        Response response = execute(
            "select c.first, c.id, c.score ,c.last ,s.name  from csv_table_test as c join side_simple as s on c.id = s.id");
        assert response.isSuccess();
    }

    @Test
    public void nest_mysql() throws Exception {
        Response response = execute(
            "select * from (select c.first, c.id, c.score ,c.last ,s.name  from csv_table_test as c join side_simple as s on c.id = s.id where c.score > 1 and s.name like 'z%') as d ");
        assert response.isSuccess();
    }

    Response execute(String sql) throws Exception {
        File file = ResourceUtils.getFile("classpath:mysql/side.yaml");
        SqlSubmitFlinkRequest sqlSubmitFlinkRequest
            = BindPropertiesUtils.bindProperties(file, SqlSubmitFlinkRequest.class);
        sqlSubmitFlinkRequest.setTest(true);
        sqlSubmitFlinkRequest.getTable().setSql(sql);
        sqlSubmitFlinkRequest.setJobName("test_mysql_side");
        return this.cluster.send(sqlSubmitFlinkRequest);
    }

}
