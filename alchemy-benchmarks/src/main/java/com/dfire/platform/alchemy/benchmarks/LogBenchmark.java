/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dfire.platform.alchemy.benchmarks;



import com.dfire.platform.alchemy.benchmarks.inner.EmptyTableSink;
import com.dfire.platform.alchemy.benchmarks.inner.LogTableSource;
import com.dfire.platform.alchemy.benchmarks.generate.NginxGenerateLog;
import com.dfire.platform.alchemy.formats.grok.GrokRowDeserializationSchema;
import com.dfire.platform.alchemy.function.logstash.*;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.runner.Runner;

import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;


@OperationsPerInvocation(value = LogBenchmark.RECORDS_PER_INVOCATION)
public class LogBenchmark extends BenchmarkBase {

	public static final int RECORDS_PER_INVOCATION = 1_00_000;

	public static  boolean LOCAL = false;

	public static void main(String[] args)
			throws Exception {
		if (args.length > 0) {
			LOCAL = Boolean.valueOf(args[0]);
		}
		if (LOCAL) {
			FlinkEnvironmentContext flinkEnvironmentContext = new FlinkEnvironmentContext();
			flinkEnvironmentContext.setUp();
			execute(flinkEnvironmentContext);
			Metric.out();
		}else {
			Options options = new OptionsBuilder()
					.verbosity(VerboseMode.NORMAL)
					.include(".*" + LogBenchmark.class.getSimpleName() + ".*")
					.build();

			new Runner(options).run();
		}
	}

	@Benchmark
	public void nginxBenchmark(FlinkEnvironmentContext context) throws Exception {
		execute(context);
	}

	public static void execute(FlinkEnvironmentContext context) throws Exception {
		String regular="\\[%{DATA:syslog_host}\\] \\[%{DATA:syslog_tag}\\] \"%{IP:remote_addr}\" \"%{DATA:remote_user}\" \"\\[%{HTTPDATE:time_local}\\]\" \"%{WORD:http_method} %{URIPATH:request}(?:\\?%{DATA:request_param}|) HTTP\\/%{NUMBER:http_version:float}\" \"(?:%{NUMBER:status:integer}|-)\" \"(?:%{NUMBER:body_bytes_sent:integer}|-)\" \"%{DATA:http_referer}\" \"%{DATA:user_agent}\" \"%{WORD} %{DATA:url}\" \"%{DATA:host_name}\" \"%{DATA:http_x_forwarded_for}\" \"(?:%{NUMBER:request_time:float}|%{DATA})\" \"%{DATA:remote_port}\" \"(?:%{DATA:upstream_response_time}|-)\" \"%{DATA:http_x_readtime}\" \"(?:/%{DATA:uri}|%{DATA})\" \"%{DATA:upstream_status}\" \"%{DATA:upstream_addr}\" \"%{DATA:nuid}\" \"%{DATA:request_id}\" \"%{DATA:http_x_forwarded_proto}\"";
		StreamExecutionEnvironment execEnv = context.env;
		StreamTableEnvironment env = StreamTableEnvironment.getTableEnvironment(execEnv);
		String[] columnNames = getNginxColumns();
		TypeInformation[] columnTypes = getnNginxColumnTypes();
		TableSchema tableSchema=new TableSchema(columnNames, columnTypes);
		TypeInformation<Row> returnType = new RowTypeInfo(columnTypes, columnNames);
		GrokRowDeserializationSchema deserializationSchema=new GrokRowDeserializationSchema(returnType, regular);
		env.registerTableSource("ngx_log",new LogTableSource(deserializationSchema,tableSchema,returnType, new NginxGenerateLog(), Long.valueOf(RECORDS_PER_INVOCATION)));
		registerFunction(env);
		Table table = env.sqlQuery(createSql());
		table.writeToSink(new EmptyTableSink());
		execEnv.execute("logSqlJob");
	}

	private static String createSql(){
		return "SELECT message_final AS message\n" +
				"\t, URLDECODE(MAPCHANGE(kvmap_U_p, 'param_U_p_nickname', ' URLDECODE')) AS param_U_p_nickname\n" +
				"\t, URLDECODE(MAPCHANGE(kvmap_U_p, 'param_U_p_shop_name', ' URLDECODE')) AS param_U_p_shop_name\n" +
				"\t, REMOVE(kvmap_U_p, 'param_U_p_shop_name', 'param_U_p_nickname') AS kvmap_U_p_final\n" +
				"\t, kvmap_geoip, param_fpt, param_U, param_res, requestd\n" +
				"\t, REMOVE(kvmap_param, 'param_fpt', 'param_U', 'param_res') AS kvmap_param_final\n" +
				"\t, kvmap_user_agent, kvmap_param_U, http_url, request, user_agent\n" +
				"\t, DATEFORMAT(time_local, 'dd/MMM/yyyy:HH:mm:ss Z') AS dateformat, syslog_host\n" +
				"\t, syslog_tag, remote_addr, remote_user, time_local, http_method\n" +
				"\t, request_param, http_version, status, body_bytes_sent, http_referer\n" +
				"\t, host_name, http_x_forwarded_for, request_time, remote_port, upstream_response_time\n" +
				"\t, http_x_readtime, uri, upstream_status, upstream_addr, nuid\n" +
				"\t, request_id, http_x_forwarded_proto\n" +
				"FROM (\n" +
				"\tSELECT *\n" +
				"\t\t, MAPKVCHANGE(kvmap_param_U, 'param_U_p', 'KV', '&', 'param_U_p_') AS kvmap_U_p\n" +
				"\t\t, GEOIP(client_ip) AS kvmap_geoip\n" +
				"\t\t, CASE \n" +
				"\t\t\tWHEN message IS NOT NULL\n" +
				"\t\t\tAND CHAR_LENGTH(message) > 2048 THEN SUBSTRING(message, 1, 2048)\n" +
				"\t\t\tELSE message\n" +
				"\t\tEND AS message_final\n" +
				"\tFROM (\n" +
				"\t\tSELECT *, GROK(param_U, '%{DATA:param_U_url}\\\\?%{GREEDYDATA:param_U_p}') AS kvmap_param_U\n" +
				"\t\t\t, CASE \n" +
				"\t\t\t\tWHEN client_ip_gsub = http_x_forwarded_for THEN client_ip_gsub\n" +
				"\t\t\t\tELSE ''\n" +
				"\t\t\tEND AS client_ip\n" +
				"\t\tFROM (\n" +
				"\t\t\tSELECT *\n" +
				"\t\t\t\t, GSUB(GSUB(client_ip_http, '(^100|^172|^10|^192|^127)\\\\..?((, *)|$)|(unknown, *)', ''), '(,.*)|( *)', '') AS client_ip_gsub\n" +
				"\t\t\t\t, MAPCHANGE(kvmap_param, 'param_fpt', 'CHANGE', 'integer') AS param_fpt\n" +
				"\t\t\t\t, URLDECODE(MAPCHANGE(kvmap_param, 'param_U', ' URLDECODE')) AS param_U\n" +
				"\t\t\t\t, URLDECODE(MAPCHANGE(kvmap_param, 'param_res', ' URLDECODE')) AS param_res\n" +
				"\t\t\t\t, SPLIT(request, '/') AS requestd\n" +
				"\t\t\t\t, GROK(user_agent_grok, '%{DATA}NetType/%{DATA:ua_net_type} Language/%{GREEDYDATA:ua_language}') AS kvmap_user_agent\n" +
				"\t\t\tFROM (\n" +
				"\t\t\t\tSELECT log.*, ua.*\n" +
				"\t\t\t\t\t, KV(log.request_param, '&', 'param_') AS kvmap_param\n" +
				"\t\t\t\t\t, CASE \n" +
				"\t\t\t\t\t\tWHEN log.url IS NOT NULL\n" +
				"\t\t\t\t\t\tAND log.url <> '-' THEN log.url\n" +
				"\t\t\t\t\t\tELSE ''\n" +
				"\t\t\t\t\tEND AS http_url\n" +
				"\t\t\t\t\t, CASE \n" +
				"\t\t\t\t\t\tWHEN log.request IS NOT NULL\n" +
				"\t\t\t\t\t\tAND log.request <> '/' THEN log.request\n" +
				"\t\t\t\t\t\tELSE ''\n" +
				"\t\t\t\t\tEND AS request\n" +
				"\t\t\t\t\t, CASE \n" +
				"\t\t\t\t\t\tWHEN log.http_x_forwarded_for IS NOT NULL\n" +
				"\t\t\t\t\t\tAND log.http_x_forwarded_for <> '-' THEN log.http_x_forwarded_for\n" +
				"\t\t\t\t\t\tELSE ''\n" +
				"\t\t\t\t\tEND AS client_ip_http\n" +
				"\t\t\t\t\t, CASE \n" +
				"\t\t\t\t\t\tWHEN log.user_agent LIKE 'NetType' THEN log.user_agent\n" +
				"\t\t\t\t\t\tELSE ''\n" +
				"\t\t\t\t\tEND AS user_agent_grok\n" +
				"\t\t\t\tFROM ngx_log log\n" +
				"              \t,LATERAL TABLE(USERAGENT(log.user_agent)) as ua \n" +
				"\t\t\t) ngx_log_tmp\n" +
				"\t\t) ngx_log_tmp_one\n" +
				"\t) ngx_log_tmp_two\n" +
				") sg_ngx_acc";
	}


	private static TypeInformation[] getnNginxColumnTypes() {
		TypeInformation[] typeInformations = new TypeInformation[27];
		typeInformations[0] = Types.STRING;
		typeInformations[1] = Types.STRING;
		typeInformations[2] = Types.STRING;
		typeInformations[3] = Types.STRING;
		typeInformations[4] = Types.STRING;
		typeInformations[5] = Types.STRING;
		typeInformations[6] = Types.STRING;
		typeInformations[7] = Types.STRING;
		typeInformations[8] = Types.FLOAT;
		typeInformations[9] = Types.INT;
		typeInformations[10] = Types.INT;
		typeInformations[11] = Types.STRING;
		typeInformations[12] = Types.STRING;
		typeInformations[13] = Types.STRING;
		typeInformations[14] = Types.STRING;
		typeInformations[15] = Types.STRING;
		typeInformations[16] = Types.FLOAT;
		typeInformations[17] = Types.STRING;
		typeInformations[18] = Types.STRING;
		typeInformations[19] = Types.STRING;
		typeInformations[20] = Types.STRING;
		typeInformations[21] = Types.STRING;
		typeInformations[22] = Types.STRING;
		typeInformations[23] = Types.STRING;
		typeInformations[24] = Types.STRING;
		typeInformations[25] = Types.STRING;
		typeInformations[26] = Types.STRING;
		return typeInformations;
	}

	private static String[] getNginxColumns() {
		String[] columns = new String[27];
		columns[0] = "syslog_host";
		columns[1] = "syslog_tag";
		columns[2] = "remote_addr";
		columns[3] = "remote_user";
		columns[4] = "time_local";
		columns[5] = "http_method";
		columns[6] = "request";
		columns[7] = "request_param";
		columns[8] = "http_version";
		columns[9] = "status";
		columns[10] = "body_bytes_sent";
		columns[11] = "http_referer";
		columns[12] = "user_agent";
		columns[13] = "url";
		columns[14] = "host_name";
		columns[15] = "http_x_forwarded_for";
		columns[16] = "request_time";
		columns[17] = "remote_port";
		columns[18] = "upstream_response_time";
		columns[19] = "http_x_readtime";
		columns[20] = "uri";
		columns[21] = "upstream_status";
		columns[22] = "upstream_addr";
		columns[23] = "nuid";
		columns[24] = "request_id";
		columns[25] = "http_x_forwarded_proto";
		columns[26] = "message";
		return columns;
	}

	private static void registerFunction(StreamTableEnvironment env) {
		GeoIpFunction geoIpFunction = new GeoIpFunction();
		env.registerFunction(geoIpFunction.getFunctionName() , geoIpFunction);
		UserAgentFunction userAgentFunction = new UserAgentFunction();
		env.registerFunction(userAgentFunction.getFunctionName() , userAgentFunction);
		KvFunction kvFunction = new KvFunction();
		env.registerFunction(kvFunction.getFunctionName() , kvFunction);
		SplitFunction splitFunction = new SplitFunction();
		env.registerFunction(splitFunction.getFunctionName() , splitFunction);
		UrldecodeFunction urldecodeFunction = new UrldecodeFunction();
		env.registerFunction(urldecodeFunction.getFunctionName() , urldecodeFunction);
		ChangeFunction changeFunction = new ChangeFunction();
		env.registerFunction(changeFunction.getFunctionName() , changeFunction);
		GsubFunction gsubFunction = new GsubFunction();
		env.registerFunction(gsubFunction.getFunctionName() , gsubFunction);
		MapFunction mapFunction = new MapFunction();
		env.registerFunction(mapFunction.getFunctionName() , mapFunction);
		GrokFunction grokFunction = new GrokFunction();
		env.registerFunction(grokFunction.getFunctionName() , grokFunction);
		MapKVFunction mapKVFunction = new MapKVFunction();
		env.registerFunction(mapKVFunction.getFunctionName() , mapKVFunction);
		RemoveMapFunction removeMapFunction = new RemoveMapFunction();
		env.registerFunction(removeMapFunction.getFunctionName() , removeMapFunction);
		DateFormatFunction dateFormatFunction = new DateFormatFunction();
		env.registerFunction(dateFormatFunction.getFunctionName() , dateFormatFunction);

	}
}
