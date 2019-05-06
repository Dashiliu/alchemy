package com.dfire.platform.alchemy.connectors.redis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.twodfire.redis.RedisBaseService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;

/**
 * @author congbai
 * @date 2018/7/12
 */
public class SentinelService extends RedisBaseService {

    public void init(RedisProperties redisProperties) {
        Set<String> sentinelset = new HashSet<>(Arrays.asList(redisProperties.getSentinel().getSentinels().split(",")));
        pool = new JedisSentinelPool(redisProperties.getSentinel().getMaster(), sentinelset,
            redisProperties.getConfig(), Protocol.DEFAULT_TIMEOUT, null, redisProperties.getDatabase());
    }

    public void destroy() {
        if (pool != null) {
            pool.destroy();
        }
    }

    @Override
    public Jedis getResource() {
        return pool.getResource();
    }
}
