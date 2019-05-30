package com.dfire.platform.alchemy.web.connector.filesystem;

import com.dfire.platform.alchemy.web.BaseCluster;
import com.dfire.platform.alchemy.web.cluster.flink.SqlSubmitFlinkRequest;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.util.BindPropertiesUtils;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;


/**
 * @author congbai
 * @date 2019/5/30
 */
public class FileSystemTest extends BaseCluster {

    @Test
    public void simplewrite() throws Exception {
        Response response = execute("select * from ngx_log");
        assert response.isSuccess();
    }


    Response execute(String sql) throws Exception {
        File file = ResourceUtils.getFile("classpath:file/simple_files.yaml");
        SqlSubmitFlinkRequest sqlSubmitFlinkRequest
            = BindPropertiesUtils.bindProperties(file, SqlSubmitFlinkRequest.class);
        sqlSubmitFlinkRequest.setTest(true);
        sqlSubmitFlinkRequest.getTable().setSql(sql);
        sqlSubmitFlinkRequest.setJobName("test_mysql_side");
        return this.cluster.send(sqlSubmitFlinkRequest);
    }


    @Test
    public void seqWrite(){

    }


}
