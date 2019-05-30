package com.dfire.platform.alchemy.web.connector.mysql;

import com.dfire.platform.alchemy.web.util.BindPropertiesUtils;
import com.dfire.platform.alchemy.web.cluster.flink.SqlSubmitFlinkRequest;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import org.junit.Test;
import org.springframework.util.ResourceUtils;
import com.dfire.platform.alchemy.web.BaseCluster;

import java.io.File;

/**
 * @author congbai
 * @date 2019/5/27
 */
public class MysqlSinkTest extends BaseCluster {

    @Test
    public void insert() throws Exception {
        Response response = execute("select c.id,c.first,c.last,c.score from csv_table_test as c", "classpath:mysql/insert.yaml");
        assert response.isSuccess();
    }

    @Test
    public void replace() throws Exception {
        Response response = execute("select c.id,c.first,c.last,c.score from csv_table_test as c", "classpath:mysql/replace.yaml");
        assert response.isSuccess();
    }

    Response execute(String sql,String yaml) throws Exception {
        File file = ResourceUtils.getFile(yaml);
        SqlSubmitFlinkRequest sqlSubmitFlinkRequest = BindPropertiesUtils.bindProperties(file, SqlSubmitFlinkRequest.class);
        sqlSubmitFlinkRequest.setTest(true);
        sqlSubmitFlinkRequest.getTable().setSql(sql);
        sqlSubmitFlinkRequest.setJobName("test_mysql_side");
        return this.cluster.send(sqlSubmitFlinkRequest);
    }
}
