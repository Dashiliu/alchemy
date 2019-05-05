package web.cluster;

import com.dfire.platform.alchemy.web.bind.BindPropertiesFactory;
import com.dfire.platform.alchemy.web.cluster.ClusterInfo;
import com.dfire.platform.alchemy.web.cluster.FlinkCluster;
import com.dfire.platform.alchemy.web.cluster.request.SqlSubmitFlinkRequest;
import com.dfire.platform.alchemy.web.cluster.response.Response;
import com.dfire.platform.alchemy.web.common.Constants;
import org.apache.flink.runtime.jobmanager.HighAvailabilityMode;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author congbai
 * @date 06/06/2018
 */
public class GrokFlinkClusterTest {

    private FlinkCluster cluster;

    @Before
    public void before() {
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setName("test");
        clusterInfo.setClusterId("daily-default-8");
        clusterInfo.setAddress("10.1.21.95");
        clusterInfo.setMode(HighAvailabilityMode.ZOOKEEPER.toString().toLowerCase());
        clusterInfo.setPort(6123);
        clusterInfo.setAvg("com.dfire.platform:alchemy-connectors:0.0.7");
        clusterInfo.setStoragePath("hdfs://sunset002.daily.2dfire.info:8020/flink/ha/real");
        clusterInfo.setZookeeperQuorum("10.1.22.21:2181,10.1.22.22:2181,10.1.22.23:2181");
        this.cluster = new FlinkCluster();
        cluster.start(clusterInfo);
    }

    @Test
    public void sendJar() {

    }


    @Test
    public void ngxTestSql() throws Exception {
        SqlSubmitFlinkRequest sqlSubmitRequest = createSqlRequest(

            "SELECT " +
                    "message_final as message" +
                    ", URLDECODE(MAPCHANGE(kvmap_U_p, 'param_U_p_nickname', ' URLDECODE')) AS param_U_p_nickname" +
                    ",  URLDECODE(MAPCHANGE(kvmap_U_p, 'param_U_p_shop_name', ' URLDECODE')) AS param_U_p_shop_name " +
                    ",REMOVE(kvmap_U_p,'param_U_p_shop_name','param_U_p_nickname') as kvmap_U_p_final" +
                    ",kvmap_geoip" +
                    ",param_fpt" +
                    ",param_U" +
                    ",param_res" +
                    ",requestd" +
                    ",REMOVE(kvmap_param,'param_fpt','param_U','param_res') as kvmap_param_final" +
                    ",kvmap_user_agent"+
                    ",kvmap_param_U"+
                    ",http_url"+
                    ",request"+
                    ",user_agent"+
                    ",DATEFORMAT(time_local,'dd/MMM/yyyy:HH:mm:ss Z') as dateformat" +
                    ",syslog_host,syslog_tag,remote_addr,remote_user,time_local,http_method,request_param,http_version,status" +
                    ",body_bytes_sent,http_referer,host_name,http_x_forwarded_for,request_time,remote_port" +
                    ",upstream_response_time,http_x_readtime,uri,upstream_status,upstream_addr,nuid,request_id" +
                ",http_x_forwarded_proto" +
                " FROM (" +
                    "select *" +
                        ", MAPKVCHANGE(kvmap_param_U, 'param_U_p', 'KV', '&', 'param_U_p_') AS kvmap_U_p " +
                        ", GEOIP(client_ip) AS kvmap_geoip "+

                        ", CASE  " +
                        " WHEN message is not null AND CHAR_LENGTH(message)>2048 THEN SUBSTRING(message,1,2048)"+
                        " ELSE message " +

                        " END AS message_final " +

                    " FROM ( " +
                        "SELECT * " +
                            ", GROK(param_U, '%{DATA:param_U_url}\\?%{GREEDYDATA:param_U_p}') AS kvmap_param_U " +
                            ", CASE  " +
                            "WHEN client_ip_gsub = http_x_forwarded_for THEN client_ip_gsub " +
                            "ELSE '' " +
                            "END AS client_ip " +
                        "FROM ( " +
                            "SELECT * " +
                                ", GSUB(GSUB(client_ip_http, '(^100|^172|^10|^192|^127)\\..?((, *)|$)|(unknown, *)', ''), '(,.*)|( *)', '') AS client_ip_gsub\n" +
                                ", MAPCHANGE(kvmap_param, 'param_fpt', 'CHANGE', 'integer') AS param_fpt\n" +
                                ",  URLDECODE(MAPCHANGE(kvmap_param, 'param_U', ' URLDECODE')) AS param_U\n" +
                                ",  URLDECODE(MAPCHANGE(kvmap_param, 'param_res', ' URLDECODE')) AS param_res\n" +
                                ", SPLIT(request, '/') AS requestd " +
                                ", GROK(user_agent_grok, '%{DATA}NetType/%{DATA:ua_net_type} Language/%{GREEDYDATA:ua_language}') AS kvmap_user_agent\n" +
                            "FROM ( " +
                                "SELECT log.*, ua.* " +
                                    ", KV(log.request_param, '&', 'param_') AS kvmap_param " +

                                    ", CASE " +
                                    "WHEN log.url IS NOT NULL " +
                                    "AND log.url <> '-' THEN log.url " +
                                    "ELSE '' " +
                                    "END AS http_url " +

                                    ", CASE  " +
                                    "WHEN log.request IS NOT NULL " +
                                    "AND log.request <> '/' THEN log.request " +
                                    "ELSE '' " +
                                    "END AS request " +

                                    ", CASE " +
                                    "WHEN log.http_x_forwarded_for IS NOT NULL\n" +
                                    "AND log.http_x_forwarded_for <> '-' THEN log.http_x_forwarded_for\n" +
                                    "ELSE ''\n" +
                                    "END AS client_ip_http\n" +

                                    ", CASE \n" +
                                    "WHEN log.user_agent LIKE 'NetType' THEN log.user_agent\n" +
                                    "ELSE ''\n" +
                                    "END AS user_agent_grok\n" +
                                "FROM ngx_log log\n" +
                                    ",LATERAL TABLE(USERAGENT(log.user_agent)) as ua " +
                            ") ngx_log_tmp " +
                        ") ngx_log_tmp_one" +
                    ") ngx_log_tmp_two" +
                ") sg_ngx_acc"
            ,
            "flinkClusterTest-TableSQL");
        Response resp = this.cluster.send(sqlSubmitRequest);
        assert resp.isSuccess();
    }

    private SqlSubmitFlinkRequest createSqlRequest(String sql, String jobName) throws Exception {
        File file = ResourceUtils.getFile("classpath:ngx-config.yaml");
        SqlSubmitFlinkRequest sqlSubmitFlinkRequest = new SqlSubmitFlinkRequest();
        BindPropertiesFactory.bindProperties(sqlSubmitFlinkRequest, Constants.BIND_PREFIX, new FileInputStream(file));
        List<String> codes = new ArrayList<>();
        codes.add(createScalarUdfs());
        codes.add(createTableUdfs());
        codes.add(createAggreUdfs());
        codes.add(createHbaseCode());
        sqlSubmitFlinkRequest.getTable().setCodes(codes);
        sqlSubmitFlinkRequest.getTable().setSql(sql);
        sqlSubmitFlinkRequest.setJobName(jobName);
        sqlSubmitFlinkRequest.setTest(true);
        return sqlSubmitFlinkRequest;
    }

    private String createScalarUdfs() {
        return "import com.dfire.platform.alchemy.api.function.StreamScalarFunction;\n" + "\n" + "/**\n"
            + " * @author congbai\n" + " * @date 06/06/2018\n" + " */\n"
            + "public class TestFunction implements StreamScalarFunction<String> {\n" + "\n" + "    @Override\n"
            + "    public  String invoke(Object... args) {\n" + "        String result=2222;\n"
            + "        return  String.valueOf(result);\n" + "    }\n" + "}\n";
    }

    private String createTableUdfs() {
        return "import com.dfire.platform.alchemy.api.function.StreamTableFunction;\n" + "\n" + "/**\n"
            + " * @author congbai\n" + " * @date 06/06/2018\n" + " */\n"
            + "public class TestTableFunction extends StreamTableFunction<String> {\n" + "\n" + "\n" + "    @Override\n"
            + "    public void invoke(Object... args) {\n" + "        for(Object arg:args){\n"
            + "            collect(String.valueOf(arg));\n" + "        }\n" + "    }\n" + "}\n";
    }

    private String createAggreUdfs() {
        return "import java.util.ArrayList;\n" + "import java.util.List;\n" + "\n"
            + "import com.dfire.platform.alchemy.api.function.StreamAggregateFunction;\n" + "\n" + "/**\n"
            + " * @author congbai\n" + " * @date 06/06/2018\n" + " */\n"
            + "public class TestAggreFunction implements StreamAggregateFunction<String, List, Integer> {\n" + "\n"
            + "    @Override\n" + "    public List createAccumulator() {\n" + "        return new ArrayList();\n"
            + "    }\n" + "\n" + "    @Override\n" + "    public void accumulate(List accumulator, String value) {\n"
            + "        accumulator.add(value);\n" + "    }\n" + "\n" + "    @Override\n"
            + "    public Integer getValue(List accumulator) {\n" + "        return accumulator.size();\n" + "    }\n"
            + "}\n";
    }

    private String createHbaseCode() {
        return "import com.dfire.platform.alchemy.api.sink.HbaseInvoker;\n" +
            "\n" +
            "/**\n" +
            " * @author congbai\n" +
            " * @date 07/06/2018\n" +
            " */\n" +
            "public class TestHbaseInvoke implements HbaseInvoker {\n" +
            "    @Override\n" +
            "    public String getRowKey(Object[] rows) {\n" +
            "        return String.valueOf(rows[0]);\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public String getFamily(Object[] rows) {\n" +
            "        return \"s\";\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public String getQualifier(Object[] rows) {\n" +
            "        return String.valueOf(rows[0]);\n" +
            "    }\n" +
            "}\n";
    }

}
