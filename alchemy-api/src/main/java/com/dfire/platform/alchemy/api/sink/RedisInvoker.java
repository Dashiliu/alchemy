package com.dfire.platform.alchemy.api.sink;

import java.io.Serializable;
import java.util.Map;

import com.twodfire.redis.ICacheService;

/**
 * @author congbai
 * @date 06/06/2018
 */
public interface RedisInvoker extends Serializable {

    void invoke(ICacheService cacheService, Map<String, Object> values);

}
