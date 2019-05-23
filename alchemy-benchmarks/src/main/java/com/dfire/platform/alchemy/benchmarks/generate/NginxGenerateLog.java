package com.dfire.platform.alchemy.benchmarks.generate;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author congbai
 * @date 2019/4/22
 */
public class NginxGenerateLog implements GenerateLog{

    private LongAdder conut = new LongAdder();

    private static final String  MESSAGE1 = "[global-proxy004] [ngxacc:] \"112.96.183.153\" \"-\" \"[22/Apr/2019:17:37:16 +0800]\" \"POST /listCart?method=com.dfire.complex.industry.service.ICartService.listCart&industry=0&app_key=200018&s_os=iOS&s_osv=12.2&s_ep=WeChat&s_epv=7.0.3&s_sc=375*724&timestamp=1555925629470&s_web=1&v=1.0&format=json&function=list_cloud_cart&entityId=00330055 HTTP/2.0\" \"200\" \"11771\" \"https://d.2dfire.com/theme/377068829077031624-1.0.0/?hdt=1555925627930&profession=0&sdt=1555925623109&industry=0&shop_kind=1&seat_code=3&entity_id=0033";

    private static final String  MESSAGE2 = "[gray001] [ngxacc:] \"10.26.0.39\" \"-\" \"[13/May/2019:16:25:48 +0800]\" \"GET /adv/v1/statistics/STS-P002-001?entity_id=00281646&xtoken=b07997e7397455b6ab0ae895fa10ec64 HTTP/1.0\" \"200\" \"22\" \"https://d.2dfire.com/om/page/om.html?e=00281646&m=1&s=A06&t=1557041638167\" \"Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16D57 MicroMessenger/7.0.3(0x17000321) NetType/4G Language/zh_CN\" \"GET http://meal.2dfire.com/adv/v1/statistics/STS-P002-001?entity_id=00281646&xtoken=b07997e7397455b6ab0ae895fa10ec64\" \"meal.2dfire.com\" \"112.96.71.205, 218.11.4.9, 100.116.252.18\" \"0.007\" \"12168\" \"0.007\" \"-\" \"/defaultProxy/adv/v1/statistics/STS-P002-001\" \"200\" \"10.10.26.179:8080\" \"ChoAvVzZKHID4WJhxddFAg==\" \"5c5ca2be9e0182582427287867b4e28b\"";

    @Override
    public byte[] generate() {
        conut.increment();
        return conut.longValue() % 2 == 0 ? MESSAGE1.getBytes() : MESSAGE2.getBytes();
    }
}
