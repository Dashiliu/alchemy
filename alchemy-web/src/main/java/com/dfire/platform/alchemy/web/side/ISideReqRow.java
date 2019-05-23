package com.dfire.platform.alchemy.web.side;

import com.dfire.platform.alchemy.web.common.Side;
import org.apache.calcite.sql.JoinType;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.types.Row;

import java.util.List;
import java.util.Optional;

/**
 * @author congbai
 * @date 2019/5/21
 */
public interface ISideReqRow<T> {

    default Cache<T> create(Side side){
        return CacheFactory.get(side);
    }

    default Optional<T> getOrNull(Cache<T> cache , String key){
        if (cache == null){
            return null;
        }
        return cache.get(key);
    }

    default void setOrNull(Cache<T> cache , String key, Optional<T> value){
        if (cache == null){
            return;
        }
        cache.put(key, value);
    }

    default void dealMissKey(Row input , JoinType joinType , ResultFuture<Row> resultFuture){
        if(joinType == JoinType.LEFT){
            //Reserved left table data
            List<Row> row = fillData(input, null);
            resultFuture.complete(row);
        }else{
            resultFuture.complete(null);
        }
    }


    List<Row> fillData(Row input , T value);
}

