package com.dfire.platform.alchemy.web.cluster.request;

import java.util.List;

import com.dfire.platform.alchemy.web.descriptor.SinkDescriptor;
import com.dfire.platform.alchemy.web.descriptor.SourceDescriptor;
import com.dfire.platform.alchemy.web.descriptor.SqlInfoDescriptor;
import com.dfire.platform.alchemy.web.descriptor.UdfDescriptor;

/**
 * @author congbai
 * @date 04/06/2018
 */
public class SqlSubmitFlinkRequest extends AbstractSubmitRequest {

    private SqlInfoDescriptor sqlInfoDescriptor;

    private List<SourceDescriptor> inputs;

    private List<UdfDescriptor> userDefineFunctions;

    private List<SinkDescriptor> outputs;

    public SqlInfoDescriptor getSqlInfoDescriptor() {
        return sqlInfoDescriptor;
    }

    public void setSqlInfoDescriptor(SqlInfoDescriptor sqlInfoDescriptor) {
        this.sqlInfoDescriptor = sqlInfoDescriptor;
    }

    public List<SourceDescriptor> getInputs() {
        return inputs;
    }

    public void setInputs(List<SourceDescriptor> inputs) {
        this.inputs = inputs;
    }

    public List<UdfDescriptor> getUserDefineFunctions() {
        return userDefineFunctions;
    }

    public void setUserDefineFunctions(List<UdfDescriptor> userDefineFunctions) {
        this.userDefineFunctions = userDefineFunctions;
    }

    public List<SinkDescriptor> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<SinkDescriptor> outputs) {
        this.outputs = outputs;
    }

    @Override
    public String getCluster() {
        return null;
    }
}
