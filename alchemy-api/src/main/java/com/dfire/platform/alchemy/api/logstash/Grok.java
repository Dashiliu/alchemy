
package com.dfire.platform.alchemy.api.logstash;

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

	public static void main(String[] args) {
		String pattern = "\\[%{DATA:syslog_host}\\] \\[%{DATA:syslog_tag}\\] %{DATA:am_datetime} %{LOGLEVEL:am_level}%{SPACE}%{DATA:am_class}(?: traceId\\:%{DATA:traceId}|)(?: spandId\\:%{DATA:spandId}|)(?: parentId\\:%{DATA:parentId}|) (?:%{DATA:am_marker}([。|：|；])|)%{SPACE}(?:json\\:%{SPACE}%{GREEDYDATA:am_json}|%{GREEDYDATA:am_msg})(?:exception_msg\\:%{GREEDYDATA:exception}|)";
		String message = "[boss-soa002] [tag_jetty] 2019-01-10 10:16:22.423 INFO  access.invoke:85 [请求日志]。json:{\"traceId\":\"a0a173d-7587469270950078998\",\"providerIp\":\"10.26.0.120\",\"side\":\"provider\",\"method\":\"com.dfire.soa.boss.mg.service.IMessageService#getUserMessage(String,Integer,Integer)\",\"consumerIp\":\"10.10.23.61\",\"time\":3,\"providerHostName\":\"10.26.0.120\",\"consumerHostName\":\"10.10.23.61\"}";
		String pattern2 = "%{TIMESTAMP_ISO8601:timestamp1}%{SPACE}%{WORD:location}.%{WORD:level}%{SPACE}%{IP:ip}%{SPACE}%{MONTH:month}";
		String message2 = "2018-08-23 02:56:53 Local7.Info 171.8.79.214 Aug Aug  23 02:56:53 2018 "
				+ "S7506E-A %%10OSPF/6/ORIGINATE_LSA(t): OSPF TrapIDpID1.3.6.1.2.1..1.2.1.14.16.2.1.2.12ospfOriginateLsa: "
				+ "Originate new LSA AreaId 0.0.0.0 LsdbType 5 LsdbLsid id 192.168.17.16Lsd LsdbRouterId Id 192.168.250.254"
				+ " Rou Router er 192.168.250.254.";
		System.out.println(Grok.match(message, pattern));
		System.out.println(Grok.match(message2, pattern2));

	}

}
