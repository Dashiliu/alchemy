package web.connector.hbase;

import java.io.File;
import java.util.Map;

import org.junit.Test;
import org.springframework.util.ResourceUtils;

import com.dfire.platform.alchemy.web.cluster.flink.SqlSubmitFlinkRequest;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.util.BindPropertiesUtils;

import web.BaseCluster;

/**
 * @author congbai
 * @date 2019/5/28
 */
public class HbaseSinkTest extends BaseCluster {

    @Test
    public void singleFaimly() throws Exception {
        Response response = execute("select * from csv_table_test",true);
        assert response.isSuccess();
    }


    @Test
    public void multiFaimly() throws Exception {
        Response response = execute("select * from csv_table_test",false);
        assert response.isSuccess();
    }


    Response execute(String sql, boolean single) throws Exception {
        File file = ResourceUtils.getFile("classpath:hbase/hbase.yaml");
        SqlSubmitFlinkRequest sqlSubmitFlinkRequest
            = BindPropertiesUtils.bindProperties(file, SqlSubmitFlinkRequest.class);
        sqlSubmitFlinkRequest.setTest(true);
        sqlSubmitFlinkRequest.getTable().setSql(sql);
        sqlSubmitFlinkRequest.setJobName("test_mysql_side");
        Map<String,Object> redisSink = sqlSubmitFlinkRequest.getTable().getSinks().get(0);
        if (single){
            redisSink.put("familyColumns", null);
        }else{
            redisSink.put("tableName", "hbase-multi");
        }
        return this.cluster.send(sqlSubmitFlinkRequest);
    }
}
