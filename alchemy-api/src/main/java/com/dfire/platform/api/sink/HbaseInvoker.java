package com.dfire.platform.api.sink;

import java.io.Serializable;

/**
 * @author congbai
 * @date 06/06/2018
 */
public interface HbaseInvoker extends Serializable {

    String getRowKey(Object[] rows);

    String getQualifier(Object[] rows);

}
