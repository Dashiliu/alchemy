package com.dfire.platform.alchemy.connectors.redis;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dfire.platform.alchemy.api.sink.RedisInvoker;
import com.dfire.platform.alchemy.api.util.GroovyCompiler;
import com.dfire.platform.alchemy.api.util.RandomUtils;
import com.dfire.platform.alchemy.api.util.RowUtils;
import com.twodfire.redis.ICacheService;

/**
 * @author congbai
 * @date 07/06/2018
 */
public class RedisSinkFunction extends RichSinkFunction<Tuple2<Boolean, Row>> {

    protected static final Logger LOG = LoggerFactory.getLogger(RedisSinkFunction.class);

    private final RedisProperties redisProperties;

    private final String code;

    private RedisInvoker redisInvoker;

    private transient BlockingQueue<Row> queue;

    private transient ICacheService cacheService;

    private transient ExecutorService threadPool;

    private AtomicBoolean shutDown = new AtomicBoolean(false);

    public RedisSinkFunction(RedisProperties redisProperties, String code) {
        this.code = code;
        this.redisInvoker = null;
        this.redisProperties = redisProperties;
        initQueue(redisProperties);
    }

    public RedisSinkFunction(RedisProperties redisProperties, RedisInvoker redisInvoker) {
        this.code = null;
        this.redisInvoker = redisInvoker;
        this.redisProperties = redisProperties;
        initQueue(redisProperties);
    }

    private void initQueue(RedisProperties redisProperties) {
        if (redisProperties.getQueueSize() != null && redisProperties.getQueueSize() > 0
            && redisProperties.getThreadNum() != null && redisProperties.getThreadNum() > 0) {
            this.queue = new ArrayBlockingQueue<>(redisProperties.getQueueSize());
        } else {
            this.queue = null;
        }
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        if (this.redisProperties.getCodis() != null) {
            this.cacheService = initCodis();
        } else if (this.redisProperties.getSentinel() != null) {
            this.cacheService = initSentinel();
        }
        if (this.queue != null) {
            initThreadPool();
        }
        if (this.redisInvoker == null) {
            this.redisInvoker = GroovyCompiler.create(this.code, RandomUtils.uuid());
        }
        super.open(parameters);
    }

    private ICacheService initSentinel() {
        SentinelService cacheService = new SentinelService();
        cacheService.init(this.redisProperties);
        return cacheService;
    }

    private ICacheService initCodis() {
        CodisService codisService = new CodisService();
        codisService.init(this.redisProperties);
        return codisService;
    }

    private void initThreadPool() {
        this.threadPool = Executors.newFixedThreadPool(this.redisProperties.getThreadNum(), new ThreadFactory() {

            private final AtomicInteger poolNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "redisConsuemr-" + poolNumber.getAndIncrement());
                // 作为守护线程存在
                thread.setDaemon(true);
                return thread;
            }
        });
        for (int i = 0; i < this.redisProperties.getThreadNum(); i++) {
            threadPool.submit(new RedisConsumer());
        }
    }

    @Override
    public void invoke(Tuple2<Boolean, Row> value, Context context) throws Exception {
        if (value == null || value.f1 == null) {
            return;
        }
        if (this.queue == null) {
            redisInvoker.invoke(this.cacheService, RowUtils.createRows((value.f1)));
        } else {
            try {
                this.queue.put(value.f1);
            } catch (InterruptedException e) {
                LOG.error("redis Thread been Interrupted.", e);
                return;
            }
        }
    }

    @Override
    public void close() throws Exception {
        shutDown.set(true);
        if (this.threadPool != null) {
            this.threadPool.shutdown();
        }
        super.close();
    }

    class RedisConsumer implements Runnable {
        @Override
        public void run() {
            final RedisInvoker invoker = redisInvoker;
            while (!shutDown.get()) {
                try {
                    Row row = queue.take();
                    invoker.invoke(cacheService, RowUtils.createRows(row));
                } catch (InterruptedException e) {
                    shutDown.set(true);
                    LOG.info("The thread {} is interrupted", Thread.currentThread().getName());
                    break;
                }
            }
        }
    }
}
