package com.dfire.platform.web.common;

/**
 * @author congbai
 * @date 06/06/2018
 */
public class Constants {

    /**
     * 重试次数
     */
    public static final int RESTART_ATTEMPTS = 3;

    /**
     * 重试间隔
     */
    public static final long DELAY_BETWEEN_ATTEMPTS = 10000;

    /**
     * redis默认连接数
     */
    public static final int REDIS_DEFAULT_TOTAL = 10;

    /**
     * redis最大连接数
     */
    public static final int REDIS_MAX_TOTAL = 50;

    /**
     * redis默认队列
     */
    public static final int REDIS_DEFAULT_QUEUE_SIZE = 1000;

    /**
     * redis最大队列
     */
    public static final int REDIS_MAX_QUEUE_SIZE = 10000;

    /**
     * redis默认消费线程
     */
    public static final int REDIS_DEFAULT_THREAD_SIZE = 5;

    /**
     * redis最大消费线程
     */
    public static final int REDIS_MAX_THREAD_SIZE = 20;

}
