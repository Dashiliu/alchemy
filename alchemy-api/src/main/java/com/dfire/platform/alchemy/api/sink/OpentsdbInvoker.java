package com.dfire.platform.alchemy.api.sink;

import com.dfire.platform.alchemy.api.common.TsdbData;

import java.util.List;

/**
 * @author congbai
 * @date 2018/7/10
 */
public interface OpentsdbInvoker {

    List<TsdbData> create(Object[] values);

}
