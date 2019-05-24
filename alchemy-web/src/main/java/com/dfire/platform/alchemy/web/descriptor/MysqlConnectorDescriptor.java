package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.common.Field;
import com.dfire.platform.alchemy.web.common.Side;
import com.dfire.platform.alchemy.web.side.MysqlAsyncReqRow;
import com.dfire.platform.alchemy.web.side.MysqlProperties;
import com.dfire.platform.alchemy.web.side.SideTableInfo;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author congbai
 * @date 2019/5/24
 */
public class MysqlConnectorDescriptor  implements ConnectorDescriptor {

    private String url;

    private String username;

    private String password;

    private Integer maxPoolSize;

    @Override
    public <T> T buildSource(ClusterType clusterType, List<Field> schema, FormatDescriptor format) throws Exception {
        return buildSource(clusterType, schema, format, null);
    }

    @Override
    public <T, R> T buildSource(ClusterType clusterType, List<Field> schema, FormatDescriptor format, R param) throws Exception {
        if (!(param instanceof SideTableInfo)) {
            throw new IllegalArgumentException("MysqlConnectorDescriptor's param must be SideTableInfo");
        }
        SideTableInfo sideTableInfo = (SideTableInfo) param;
        if (sideTableInfo == null) {
            //todo 正常的mysql表
            return  null;
        }
        Side side = sideTableInfo.getSide();
        if (side == null) {
            throw new IllegalArgumentException("MysqlConnectorDescriptor's side info is null");
        }
        if (side.isAsync()){
            return (T) new MysqlAsyncReqRow(sideTableInfo, createProperties());
        }else{
            //todo 同步的mysql维表
        }
        return null;
    }

    private MysqlProperties createProperties(){
        MysqlProperties properties = new MysqlProperties();
        properties.setUrl(this.url);
        properties.setUsername(this.username);
        properties.setPassword(this.password);
        properties.setMaxPoolSize(this.maxPoolSize);
        return properties;
    }

    @Override
    public String getType() {
        return Constants.CONNECTOR_TYPE_VALUE_MYSQL;
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(url, "mysql的url不能为空");
        Assert.notNull(username, "mysql的userName不能为空");
        Assert.notNull(password, "mysql的password不能为空");
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
}
