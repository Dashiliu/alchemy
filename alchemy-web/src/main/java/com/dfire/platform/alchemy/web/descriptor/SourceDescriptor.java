package com.dfire.platform.alchemy.web.descriptor;

import java.util.List;
import java.util.Map;

import com.dfire.platform.alchemy.api.common.Side;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.Assert;

import com.dfire.platform.alchemy.web.util.BindPropertiesUtils;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.common.Field;
import com.dfire.platform.alchemy.web.util.PropertiesUtils;

/**
 * @author congbai
 * @date 02/06/2018
 */
public class SourceDescriptor implements CoreDescriptor {

    private String name;

    private List<Field> schema;

    private Map<String, Object> connector;

    private volatile ConnectorDescriptor connectorDescriptor;

    private Side side;

    private FormatDescriptor format;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public <T> T transform(ClusterType clusterType) throws Exception {
        if (ClusterType.FLINK.equals(clusterType)) {
            return transformFlink();
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);
    }

    @Override
    public <T, R> T transform(ClusterType clusterType, R param) throws Exception {
        if (ClusterType.FLINK.equals(clusterType)) {
            return transformFlink(param);
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);
    }

    public List<Field> getSchema() {
        return schema;
    }

    public void setSchema(List<Field> schema) {
        this.schema = schema;
    }

    public Map<String, Object> getConnector() {
        return connector;
    }

    public void setConnector(Map<String, Object> connector) {
        this.connector = connector;
    }

    public ConnectorDescriptor getConnectorDescriptor() {
        if (this.connectorDescriptor == null) {
            synchronized (this) {
                if (this.connector == null) {
                    return this.connectorDescriptor;
                }
                Object type = this.connector.get(Constants.DESCRIPTOR_TYPE_KEY);
                if (type == null) {
                    return this.connectorDescriptor;
                }
                ConnectorDescriptor connectorDescriptor
                    = DescriptorFactory.me.find(String.valueOf(type), ConnectorDescriptor.class);
                if (connectorDescriptor == null) {
                    return this.connectorDescriptor;
                }
                try {
                    this.connectorDescriptor = connectorDescriptor.getClass().newInstance();
                    BeanUtils.populate(this.connectorDescriptor, this.connector);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return connectorDescriptor;
    }

    public void setConnectorDescriptor(ConnectorDescriptor connectorDescriptor) {
        this.connectorDescriptor = connectorDescriptor;
    }

    public FormatDescriptor getFormat() {
        return format;
    }

    public void setFormat(FormatDescriptor format) {
        this.format = format;
    }

    @Override
    public String getType() {
        return Constants.TYPE_VALUE_SOURCE;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(name, "table的名称不能为空");
        Assert.notNull(schema, "table字段不能为空");
        Assert.isTrue(schema.size() > 0, "table字段不能为空");
        ConnectorDescriptor connectorDescriptor = getConnectorDescriptor();
        if (connectorDescriptor == null) {
            throw new IllegalArgumentException("必须配置connector");
        }
        connectorDescriptor.validate();
    }

    private <T> T transformFlink() throws Exception {
        return transformFlink(null);
    }

    private <T, R> T transformFlink(R param) throws Exception {
        if (this.getConnectorDescriptor() != null) {
            return this.getConnectorDescriptor().buildSource(ClusterType.FLINK, this.schema, this.format, param);
        }
        return null;
    }
}
