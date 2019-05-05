package com.dfire.platform.alchemy.benchmarks;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author congbai
 * @date 09/05/2018
 */
public class Metric {

    public static long maxTime = 0;
    public static long minTime = -1;
    public static LongAdder count = new LongAdder();
    public static LongAdder[] responseSpreads = new LongAdder[9];

    static {
        responseSpreads[0] = new LongAdder();
        responseSpreads[1] = new LongAdder();
        responseSpreads[2] = new LongAdder();
        responseSpreads[3] = new LongAdder();
        responseSpreads[4] = new LongAdder();
        responseSpreads[5] = new LongAdder();
        responseSpreads[6] = new LongAdder();
        responseSpreads[7] = new LongAdder();
        responseSpreads[8] = new LongAdder();
    }

    public static void out(){
        out(responseSpreads);
    }

    public static void out(LongAdder[] responseSpreads) {
        System.out.println("<0:" + responseSpreads[0].longValue());
        System.out.println("0-10:" + responseSpreads[1].longValue());
        System.out.println("10-50:" + responseSpreads[2].longValue());
        System.out.println("50-100:" + responseSpreads[3].longValue());
        System.out.println("100-500:" + responseSpreads[4].longValue());
        System.out.println("500-1000:" + responseSpreads[5].longValue());
        System.out.println("1000-5000:" + responseSpreads[6].longValue());
        System.out.println("5000-10000:" + responseSpreads[7].longValue());
        System.out.println(">10000:" + responseSpreads[8].longValue());

    }

    public static void sumResponseTimeSpread(long responseTime) {
        count.increment();
        if(count.longValue() < 100000) {
            //预热
            return;
        }
        if (responseTime <= 0) {
            responseSpreads[0].increment();
        } else if (responseTime > 0 && responseTime <= 10) {
            responseSpreads[1].increment();
        } else if (responseTime > 10 && responseTime <= 50) {
            responseSpreads[2].increment();
        } else if (responseTime > 50 && responseTime <= 100) {
            responseSpreads[3].increment();
        } else if (responseTime > 100 && responseTime <= 500) {
            responseSpreads[4].increment();
        } else if (responseTime > 500 && responseTime <= 1000) {
            responseSpreads[5].increment();
        } else if (responseTime > 1000 && responseTime <= 5000) {
            responseSpreads[6].increment();
        } else if (responseTime > 5000 && responseTime <= 10000) {
            responseSpreads[7].increment();
        } else if (responseTime > 10000) {
            responseSpreads[8].increment();
        }
    }
}
