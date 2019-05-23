package com.dfire.platform.alchemy.web.side;

import com.dfire.platform.alchemy.web.common.CacheType;
import com.dfire.platform.alchemy.web.common.Side;
import org.apache.flink.table.shaded.org.apache.commons.lang3.StringUtils;



/**
 * @author congbai
 * @date 2019/5/23
 */
public class CacheFactory {

    public static <T> Cache<T> get(Side side){
        if(side == null || StringUtils.isEmpty(side.getCacheType())){
            return null;
        }
        if (StringUtils.equalsIgnoreCase(CacheType.LRU.getType(), side.getCacheType())){
            return new LruCache<>(side.getCacheSize(), side.getTtl());
        }
        return new AllCache<>();
    }


}
