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
import com.twodfire.redis.RedisSentinelService;

/**
 * @author congbai
 * @date 07/06/2018
 */
public class RedisSinkFunction extends RichSinkFunction<Tuple2<Boolean, Row>> {

    protected static final Logger LOG = LoggerFactory.getLogger(RedisSinkFunction.class);

    private final String sentinels;

    private final String master;

    private final int database;

    private final Integer maxTotal;

    private final Integer threadNum;

    private final String code;

    private RedisInvoker redisInvoker;

    private transient final BlockingQueue<Row> queue;

    protected  transient RedisSentinelService cacheService;

    private transient ExecutorService threadPool;

    private AtomicBoolean shutDown = new AtomicBoolean(false);

    public RedisSinkFunction(String sentinels, String master, int database, Integer maxTotal, Integer queueSize, Integer threadNum, String code) {
        this.sentinels = sentinels;
        this.master = master;
        this.database = database;
        this.maxTotal = maxTotal;
        this.threadNum = threadNum;
        this.code = code;
        this.redisInvoker=null;
        if(queueSize!=null&&queueSize>0){
            this.queue = new ArrayBlockingQueue<>(queueSize);
        }else{
            this.queue=null;
        }
    }

    public RedisSinkFunction(String sentinels, String master, int database, Integer maxTotal, Integer queueSize, Integer threadNum, RedisInvoker redisInvoker) {
        this.sentinels = sentinels;
        this.master = master;
        this.database = database;
        this.maxTotal = maxTotal;
        this.threadNum = threadNum;
        this.code = null;
        this.redisInvoker = redisInvoker;
        if(queueSize!=null&&queueSize>0){
            this.queue = new ArrayBlockingQueue<>(queueSize);
        }else{
            this.queue=null;
        }
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        this.cacheService = new RedisSentinelService();
        cacheService.setDatabase(database);
        cacheService.setMasterName(master);
        cacheService.setSentinels(sentinels);
        cacheService.setMaxIdle(maxTotal);
        cacheService.setMinIdle(maxTotal);
        cacheService.setMaxTotal(maxTotal);
        cacheService.init();
        if(this.queue!=null){
            initThreadPool();
        }
        if (this.redisInvoker == null) {
            this.redisInvoker = GroovyCompiler.create(this.code, RandomUtils.uuid());
        }
        super.open(parameters);
    }

    private void initThreadPool() {
        this.threadPool = Executors.newFixedThreadPool(threadNum, new ThreadFactory() {

            private final AtomicInteger poolNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "redisConsuemr-" + poolNumber.getAndIncrement());
                // 作为守护线程存在
                thread.setDaemon(true);
                return thread;
            }
        });
        for (int i = 0; i < threadNum; i++) {
            threadPool.submit(new RedisConsumer());
        }
    }

    @Override
    public void invoke(Tuple2<Boolean, Row> value, Context context) throws Exception {
        if(value==null||value.f1==null){
            return;
        }
        if(this.queue==null){
            redisInvoker.invoke(this.cacheService,RowUtils.createRows((value.f1)));
        }else{
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
        if(this.threadPool!=null){
            this.threadPool.shutdown();
        }
        super.close();
    }

    class RedisConsumer implements Runnable {
        @Override
        public void run() {
            final RedisInvoker invoker=redisInvoker;
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
