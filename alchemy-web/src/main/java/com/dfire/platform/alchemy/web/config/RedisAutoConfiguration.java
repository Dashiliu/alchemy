/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.dfire.platform.alchemy.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.twodfire.redis.ICacheService;
import com.twodfire.redis.RedisSentinelService;

import redis.clients.jedis.Jedis;

@Configuration
@ConditionalOnClass({Jedis.class})
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {

    private final RedisProperties properties;

    public RedisAutoConfiguration(RedisProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ICacheService cacheService() {
        RedisSentinelService redisSentinelService = new RedisSentinelService();
        redisSentinelService.setDatabase(properties.getDatabase());
        redisSentinelService.setMasterName(properties.getSentinel().getMaster());
        redisSentinelService.setSentinels(properties.getSentinel().getNodes().get(0));
        redisSentinelService.setMaxIdle(properties.getJedis().getPool().getMaxIdle());
        redisSentinelService.setMaxTotal(properties.getJedis().getPool().getMaxActive());
        redisSentinelService.setMinIdle(properties.getJedis().getPool().getMinIdle());
        redisSentinelService.setMinEvictableIdleTimeMillis(6000);
        redisSentinelService.setTestOnBorrow(true);
        return redisSentinelService;
    }

}
