package com.dfire.platform.alchemy.connectors.redis;

import com.twodfire.redis.RedisBaseService;

import io.codis.jodis.RoundRobinJedisPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

/**
 * @author congbai
 * @date 2018/7/12
 */
public class CodisService extends RedisBaseService {

    private RoundRobinJedisPool roundRobinJedisPool;

    public void init(RedisProperties redisProperties){
        Codis codis=redisProperties.getCodis();
        roundRobinJedisPool = RoundRobinJedisPool.create().
                curatorClient(codis.getZkAddrs(),codis.getZkSessionTimeoutMs()).
                zkProxyDir("/jodis/"+codis.getCodisProxyName()).
                poolConfig(redisProperties.getConfig()).
                database( redisProperties.getDatabase() ).
                password(codis.getPassoword()).
                timeoutMs(Protocol.DEFAULT_TIMEOUT).
                build();

    }

    public void destroy() {
        if( roundRobinJedisPool != null ){
            roundRobinJedisPool.close();
        }
    }

    @Override
    public Jedis getResource() {
        return roundRobinJedisPool.getResource();
    }
}
