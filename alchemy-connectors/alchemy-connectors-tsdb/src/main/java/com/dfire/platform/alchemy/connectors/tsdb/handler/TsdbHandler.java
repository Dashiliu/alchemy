package com.dfire.platform.alchemy.connectors.tsdb.handler;

import java.io.IOException;

import com.dfire.platform.alchemy.connectors.tsdb.TsdbData;

/**
 * @author congbai
 * @date 2018/8/8
 */
public interface TsdbHandler{

    void execute(TsdbData tsdbData);

    void close() throws IOException;
}
