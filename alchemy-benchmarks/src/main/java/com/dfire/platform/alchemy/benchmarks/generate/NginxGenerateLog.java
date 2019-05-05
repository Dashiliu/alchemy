package com.dfire.platform.alchemy.benchmarks.generate;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author congbai
 * @date 2019/4/22
 */
public class NginxGenerateLog implements GenerateLog{

    private LongAdder conut = new LongAdder();

    private static final String  MESSAGE1 = "[global-proxy004] [ngxacc:] \"112.96.183.153\" \"-\" \"[22/Apr/2019:17:37:16 +0800]\" \"POST /listCart?method=com.dfire.complex.industry.service.ICartService.listCart&industry=0&app_key=200018&s_os=iOS&s_osv=12.2&s_ep=WeChat&s_epv=7.0.3&s_sc=375*724&timestamp=1555925629470&s_web=1&v=1.0&format=json&function=list_cloud_cart&entityId=00330055 HTTP/2.0\" \"200\" \"11771\" \"https://d.2dfire.com/theme/377068829077031624-1.0.0/?hdt=1555925627930&profession=0&sdt=1555925623109&industry=0&shop_kind=1&seat_code=3&entity_id=0033";

    private static final String  MESSAGE2 = "[global-proxy004] [ngxacc:] \"106.47.31.40\" \"-\" \"[22/Apr/2019:17:48:47 +0800]\" \"POST /?method=com.dfire.kds.getInstanceSplitList&s_eid=00315247&sign=12d24abbf8dc2210aefc203d87c001a7&s_br=rk312x&s_osv=22&timestamp=1555926527&s_did=4d8c50a7bd43d389eadf937f008298b2&s_net=1&format=json&s_apv=1.3.44&v=1.0&app_key=200020&sign_method=md5&s_sc=720*1366&s_os=android HTTP/2.0\" \"200\" \"69\" \"-\" \"okhttp/3.10.0\" \"POST -\" \"gateway.2dfire.com\" \"-\" \"0.012\" \"2090\" \"0.012\" \"-\" \"/\" \"200\" \"10.10.26.214:8080\" \"-\" \"4b4dad1909830147bf7ad69120e7b35a\" \"-\"";

    @Override
    public byte[] generate() {
        conut.increment();
        return conut.longValue() % 2 == 0 ? MESSAGE1.getBytes() : MESSAGE2.getBytes();
    }
}
