package com.dfire.platform.alchemy.web.cluster.request;

import com.dfire.platform.alchemy.web.descriptor.TableDescriptor;

/**
 * @author congbai
 * @date 04/06/2018
 */
public class SqlSubmitFlinkRequest extends AbstractSubmitRequest {

    private TableDescriptor tableDescriptor;

    public TableDescriptor getTableDescriptor() {
        return tableDescriptor;
    }

    public void setTableDescriptor(TableDescriptor tableDescriptor) {
        this.tableDescriptor = tableDescriptor;
    }
}
