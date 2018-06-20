package com.dfire.platform.web.configuration;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dfire.soa.flame.FlameFactory;
import com.dfire.soa.flame.UniqueIdGenerator;

/**
 * Created by yuntun on 2018/4/16 0016.
 */
@Component
public class Flame implements InitializingBean {

    @Reference
    private UniqueIdGenerator uniqueIdGenerator;

    private FlameFactory flameFactory;

    @Value("${ac.flame.maxTotal}")
    private int maxTotal;

    @Value("${ac.flame.maxWaitMillis}")
    private long maxWaitMillis;

    @Value("${ac.flame.minEvictableIdleTimeMillis}")
    private long minEvictableIdleTimeMillis;

    public Long nextId() {
        return flameFactory.nextId();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        flameFactory = FlameFactory.getInstance(uniqueIdGenerator, maxTotal, maxWaitMillis, minEvictableIdleTimeMillis);
    }
}
