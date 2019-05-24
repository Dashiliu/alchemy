package web.side;

import com.dfire.platform.alchemy.web.bind.BindPropertiesFactory;
import com.dfire.platform.alchemy.web.cluster.flink.SqlSubmitFlinkRequest;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.common.Constants;
import org.junit.Test;
import org.springframework.util.ResourceUtils;
import web.BaseCluster;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author congbai
 * @date 2019/5/24
 */
public class SideTest extends BaseCluster {

    @Test
    public void mysql() throws Exception {
        File file = ResourceUtils.getFile("classpath:side.yaml");
        SqlSubmitFlinkRequest sqlSubmitFlinkRequest = new SqlSubmitFlinkRequest();
        sqlSubmitFlinkRequest.setTest(true);
        BindPropertiesFactory.bindProperties(sqlSubmitFlinkRequest, Constants.BIND_PREFIX, new FileInputStream(file));
        sqlSubmitFlinkRequest.getTable().setSql("select c.first, c.id, c.score ,c.last ,s.name  from csv_table_test as c join side_simple as s on c.id = s.id");
        sqlSubmitFlinkRequest.setJobName("test_mysql_side");
        Response resp = this.cluster.send(sqlSubmitFlinkRequest);
        assert resp.isSuccess();
    }

}
