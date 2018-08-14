package com.dfire.platform.alchemy.connectors.tsdb.handler;

import java.io.IOException;
import java.io.Serializable;

import com.dfire.platform.alchemy.api.common.TsdbData;

/**
 * @author congbai
 * @date 2018/8/8
 */
public interface TsdbHandler{

    void execute(TsdbData tsdbData);

    void close() throws IOException;
}
