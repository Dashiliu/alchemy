package com.dfire.platform.alchemy.web.descriptor;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.io.jdbc.JDBCAppendTableSink;
import org.apache.flink.api.java.io.jdbc.JDBCAppendTableSinkBuilder;
import org.apache.flink.table.typeutils.TypeStringUtils;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;


/**
 * @author congbai
 * @date 06/06/2018
 */
public class MysqlSinkDescriptor extends SinkDescriptor {

    private static final String DEFAULT_DRIVER_NAME = "com.zaxxer.hikari.HikariDataSource";

    private String name;

    private String username;

    private String password;

    private String driverName = DEFAULT_DRIVER_NAME;

    private String url;

    private String query;

    private Integer batchSize;

    private String[] parameterTypes;

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

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        if (ClusterType.FLINK.equals(clusterType)) {
            return transformFlink();
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(url, "url不能为空");
        Assert.notNull(username, "username不能为空");
        Assert.notNull(password, "password不能为空");
        Assert.notNull(query, "sql不能为空");
    }

    private <T> T transformFlink() throws Exception {
        TypeInformation[] parameterTyeps = create();
        JDBCAppendTableSinkBuilder jdbcAppendTableSinkBuilder = JDBCAppendTableSink.builder();
        jdbcAppendTableSinkBuilder.setDrivername(this.driverName).setDBUrl(this.url).setUsername(this.username)
            .setPassword(this.password).setQuery(this.query).setParameterTypes(parameterTyeps);
        if (this.batchSize != null) {
            jdbcAppendTableSinkBuilder.setBatchSize(this.batchSize);
        }
        return (T)jdbcAppendTableSinkBuilder.build();
    }

    private TypeInformation[] create() {
        if (this.parameterTypes == null){
            return null;
        }
        TypeInformation[] parameterTypes =new TypeInformation[this.parameterTypes.length];
        for(int i = 0; i < this.parameterTypes.length ; i++){
            parameterTypes[i] = TypeStringUtils.readTypeInfo(this.parameterTypes[i]);
        }
        return parameterTypes;
    }

    @Override
    public String type() {
        return Constants.SINK_TYPE_VALUE_MYSQL;
    }
}
