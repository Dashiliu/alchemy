package com.dfire.platform.alchemy.web.connector.tsdb;

import java.io.File;

import org.junit.Test;
import org.springframework.util.ResourceUtils;

import com.dfire.platform.alchemy.web.cluster.flink.SqlSubmitFlinkRequest;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.util.BindPropertiesUtils;

import com.dfire.platform.alchemy.web.BaseCluster;

/**
 * @author congbai
 * @date 2019/5/29
 */
public class TsdbTest extends BaseCluster {

    @Test
    public void write() throws Exception {
        Response response = execute("select * from csv_table_test");
        assert response.isSuccess();
    }

    Response execute(String sql) throws Exception {
        File file = ResourceUtils.getFile("classpath:tsdb/tsdb.yaml");
        SqlSubmitFlinkRequest sqlSubmitFlinkRequest
            = BindPropertiesUtils.bindProperties(file, SqlSubmitFlinkRequest.class);
        sqlSubmitFlinkRequest.setTest(true);
        sqlSubmitFlinkRequest.getTable().setSql(sql);
        sqlSubmitFlinkRequest.setJobName("test_tsdb_side");
        return this.cluster.send(sqlSubmitFlinkRequest);
    }

}
