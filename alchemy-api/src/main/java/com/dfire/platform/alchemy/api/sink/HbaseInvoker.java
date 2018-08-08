package com.dfire.platform.alchemy.api.sink;

import java.io.Serializable;

/**
 * @author congbai
 * @date 06/06/2018
 */
public interface HbaseInvoker extends Serializable {

    String getRowKey(Object[] rows);

    String getFamily(Object[] rows);

    String getQualifier(Object[] rows);

}
