
package com.dfire.platform.alchemy.api.logstash;

import com.dtstack.jlogstash.date.DateParser;
import com.dtstack.jlogstash.date.FormatParser;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;

import java.util.Map;

/****
 * grok {
       match => ["message", "\[%{DATA:syslog_host}\] \[%{DATA:syslog_tag}\] %{DATA:am_datetime} %{LOGLEVEL:am_level}%{SPACE}%{DATA:am_class}(?: traceId\:%{DATA:traceId}|)(?: spandId\:%{DATA:spandId}|)(?: parentId\:%{DATA:parentId}|) (?:%{DATA:am_marker}([。|：|；])|)%{SPACE}(?:json\:%{SPACE}%{GREEDYDATA:am_json}|%{GREEDYDATA:am_msg})(?:exception_msg\:%{GREEDYDATA:exception}|)"]
 }
 */
public class Grok {

	public static Map<String, Object> match(String message, String pattern) {
		GrokCompiler grokCompiler = GrokCompiler.newInstance();
		// 进行注册, registerDefaultPatterns()方法注册的是Grok内置的patterns
		grokCompiler.registerDefaultPatterns();
        /*
         传入自定义的pattern, 会从已注册的patterns里面进行配对, 例如: TIMESTAMP_ISO8601:timestamp1, TIMESTAMP_ISO8601在注册的
         patterns里面有对应的解析格式, 配对成功后, 会在match时按照固定的解析格式将解析结果存入map中, 此处timestamp1作为输出的key
          */
		io.krakens.grok.api.Grok grok = grokCompiler.compile(pattern);
		Match grokMatch = grok.match(message);
		Map<String, Object> resultMap = grokMatch.capture();
		return resultMap;
	}

//	public static void main(String[] args) {
//		String pattern = "\\[%{DATA:syslog_host}\\] \\[%{DATA:syslog_tag}\\] %{DATA:am_datetime} %{LOGLEVEL:am_level}%{SPACE}%{DATA:am_class}(?: traceId\\:%{DATA:traceId}|)(?: spandId\\:%{DATA:spandId}|)(?: parentId\\:%{DATA:parentId}|) (?:%{DATA:am_marker}([。|：|；])|)%{SPACE}(?:json\\:%{SPACE}%{GREEDYDATA:am_json}|%{GREEDYDATA:am_msg})(?:exception_msg\\:%{GREEDYDATA:exception}|)";
//		String message = "[boss-soa002] [tag_jetty] 2019-01-10 10:16:22.423 INFO  access.invoke:85 [请求日志]。json:{\"traceId\":\"a0a173d-7587469270950078998\",\"providerIp\":\"10.26.0.120\",\"side\":\"provider\",\"method\":\"com.dfire.soa.boss.mg.service.IMessageService#getUserMessage(String,Integer,Integer)\",\"consumerIp\":\"10.10.23.61\",\"time\":3,\"providerHostName\":\"10.26.0.120\",\"consumerHostName\":\"10.10.23.61\"}";
//		String pattern2 = "%{TIMESTAMP_ISO8601:timestamp1}%{SPACE}%{WORD:location}.%{WORD:level}%{SPACE}%{IP:ip}%{SPACE}%{MONTH:month}";
//		String message2 = "2018-08-23 02:56:53 Local7.Info 171.8.79.214 Aug Aug  23 02:56:53 2018 "
//				+ "S7506E-A %%10OSPF/6/ORIGINATE_LSA(t): OSPF TrapIDpID1.3.6.1.2.1..1.2.1.14.16.2.1.2.12ospfOriginateLsa: "
//				+ "Originate new LSA AreaId 0.0.0.0 LsdbType 5 LsdbLsid id 192.168.17.16Lsd LsdbRouterId Id 192.168.250.254"
//				+ " Rou Router er 192.168.250.254.";
//
//		String message3 = "[global-proxy003] [ngxacc:] \"100.116.234.76\" \"-\" \"[26/Mar/2019:16:51:46 +0800]\" \"GET /d?url=server.2dfire.com HTTP/1.1\" \"200\" \"96\" \"-\" \"Dalvik/1.6.0 (Linux; U; Android 4.4.2; rk3188 Build/KOT49H)\" \"GET -\" \"120.55.199.20\" \"39.187.114.9\" \"0.004\" \"47020\" \"0.004\" \"-\" \"/d\" \"200\" \"10.25.0.152:6789\" \"-\" \"3b570966015e31c807f9008b56852765\" \"http\"";
//		String pattern3 = "\\[%{DATA:syslog_host}\\] \\[%{DATA:syslog_tag}\\] \"%{IP:remote_addr}\" \"%{DATA:remote_user}\" \"\\[%{HTTPDATE:time_local}\\]\" \"%{WORD:http_method} %{URIPATH:request}(?:\\?%{DATA:request_param}|) HTTP\\/%{NUMBER:http_version:float}\" \"(?:%{NUMBER:status:float}|-)\" \"(?:%{NUMBER:body_bytes_sent:float}|-)\" \"%{DATA:http_referer}\" \"%{DATA:user_agent}\" \"%{WORD} %{DATA:url}\" \"%{DATA:host_name}\" \"%{DATA:http_x_forwarded_for}\" \"(?:%{NUMBER:request_time:float}|%{DATA})\" \"%{DATA:remote_port}\" \"(?:%{DATA:upstream_response_time}|-)\" \"%{DATA:http_x_readtime}\" \"(?:/%{DATA:uri}|%{DATA})\" \"%{DATA:upstream_status}\" \"%{DATA:upstream_addr}\" \"%{DATA:nuid}\" \"%{DATA:request_id}\" \"%{DATA:http_x_forwarded_proto}\"";
//		System.out.println(Grok.match(message3, pattern3));
//	}

	public static void main(String[] args) {
		String input = "30/Mar/2019:16:05:28 +0800";
        FormatParser dateParser = new FormatParser("dd/MMM/YYYY:HH:mm:ss Z",null, null);
		System.out.println(dateParser.parse(input).toString());
	}

}
