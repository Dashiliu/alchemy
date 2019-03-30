package com.dfire.platform.alchemy.api.function.aggregate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: yuntun
 * Date: 2019/3/29
 * Time: 15:21
 * Description:
 */
public class test {

    public static final String SOURCE = "30/Mar/2019:16:05:28 +0800";

    public static void main(String[] args) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "dd/MMM/yyyy:HH:mm:ss Z");


        Date date=simpleDateFormat.parse(SOURCE);
        System.out.println("。。。。"+date);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss.SSS");
        String sDate=sdf.format(date);
        System.out.println(sDate);

//        Date myDate = null;
//        try {
//            myDate = sdf.parse(SOURCE);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        System.out.println(myDate);
//
//        sdf.applyPattern("EEE MMM dd HH:mm:ss Z yyyy");
//        System.out.println(sdf.format(myDate));
//
//        SimpleDateFormat sdf2 = new SimpleDateFormat(
//                "yyyy-MM-dd HH:mm:ss", new Locale("CHINESE", "CHINA"));
//        System.out.println(sdf2.format(myDate));
//
//        sdf2.applyPattern("yyyy年MM月dd日 HH时mm分ss秒");
//        System.out.println(sdf2.format(myDate));
    }

//    select *
//            ,urldecode(mapchange(kvmap_U_p,'param_U_p_nickname','urldecode')) as param_U_p_nickname
//            ,urldecode(mapchange(kvmap_U_p,'param_U_p_shop_name','urldecode')) as param_U_p_shop_name
//
//             from  ( select *
//            ,mapkvchange(kvmap_param_U,'param_U_p','kv','&','param_U_p_') as kvmap_U_p
//             from (select
//            *,geoip(client_ip) as kvmap_geoip
//
//
//            ,grok(param_U,'%{DATA:param_U_url}\\?%{GREEDYDATA:param_U_p}') as kvmap_param_U
//
//             from (
//            select *,kvmap_param
//
//            ,CASE
//            WHEN client_ip = http_x_forwarded_for THEN client_ip
//             ELSE ''
//            END as client_ip
//
//            ,gsub(gsub(client_ip,'(^100|^172|^10|^192|^127)\\..?((, *)|$)|(unknown, *)',''),'(,.*)|( *)','') as client_ip
//
//            ,mapchange(kvmap_param,'param_fpt','change','integer') as param_fpt
//            ,urldecode(mapchange(kvmap_param,'param_U','urldecode')) as param_U
//            ,urldecode(mapchange(kvmap_param,'param_res','urldecode')) as param_res
//
//            ,split(request,'/') as requestd
//            ,grok(user_agent_grok,'%{DATA}NetType/%{DATA:ua_net_type} Language/%{GREEDYDATA:ua_language}') as kvmap_user_agent
////                ,param_u
//             from (select
//            ngx.*
//            ,ua.*
//
//            ,kv(ngx.request_param,'&','param_') as kvmap_param
//
//            ,CASE
//            WHEN ngx.url is not null AND ngx.url <> '-' THEN ngx.url
//            ELSE '' END as http_url
//
//            ,CASE
//            WHEN ngx.request is not null AND ngx.request <> '/' THEN ngx.request
//            ELSE '' END as request
//
//
//            ,CASE
//            WHEN ngx.http_x_forwarded_for is not null AND ngx.http_x_forwarded_for <> '-' THEN ngx.http_x_forwarded_for
//            ELSE '' END as client_ip
//
//            ,CASE
//            WHEN ngx.user_agent like 'NetType' THEN ngx.user_agent
//            ELSE '' END as user_agent_grok
//
//            from test_nginx_log AS ngx
//            ,LATERAL TABLE(useragent(ngx.user_agent)) as ua
////                ,LATERAL TABLE(kv(ngx.request_param,'&','param_')) as kvmap_param)
//            ) ngx_log
//            ) log ) x ) y
}
