package com.dfire.platform.alchemy.web.side;

import java.util.*;

import com.dfire.platform.alchemy.web.common.Alias;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.InferTypes;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.calcite.shaded.com.google.common.collect.Lists;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

/**
 * @author congbai
 * @date 2019/5/22
 */
public class MysqlAsyncReqRow extends AsyncReqRow<List<JsonObject>> {

    private static final Logger LOG = LoggerFactory.getLogger(MysqlAsyncReqRow.class);

    private final static String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    public final static int DEFAULT_VERTX_EVENT_LOOP_POOL_SIZE = 1;

    public final static int DEFAULT_VERTX_WORKER_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    public final static int DEFAULT_MAX_DB_CONN_POOL_SIZE
        = DEFAULT_VERTX_EVENT_LOOP_POOL_SIZE + DEFAULT_VERTX_WORKER_POOL_SIZE;

    public final static String KEY_SEP = "_";

    private final String sql;

    private transient SQLClient sqlClient;

    private MysqlProperties mysqlProperties;

    public MysqlAsyncReqRow(SideTableInfo sideTable, MysqlProperties mysqlProperties) throws SqlParseException {
        super(sideTable);
        this.mysqlProperties = mysqlProperties;
        this.sql = modifySql(sideTable);
    }

    private String modifySql(SideTableInfo sideTable) throws SqlParseException {
        SqlParser.Config config = SqlParser.configBuilder().setLex(Lex.MYSQL).build();
        SqlParser sqlParser = SqlParser.create(sideTable.getSql(), config);
        SqlNode sqlNode = sqlParser.parseStmt();
        if (SqlKind.SELECT != sqlNode.getKind()){
            throw new UnsupportedOperationException("MysqlAsyncReqRow only support query sql, sql:" + sideTable.getSql());
        }
        SqlSelect sqlSelect = (SqlSelect) sqlNode;
        SqlNode whereNode = sqlSelect.getWhere();
        SqlBinaryOperator and = new SqlBinaryOperator("AND", SqlKind.AND, 24, true, ReturnTypes.BOOLEAN_NULLABLE_OPTIMIZED, InferTypes.BOOLEAN, OperandTypes.BOOLEAN_BOOLEAN);
        List<SqlBasicCall> conditionNodes = createConditionNodes(sideTable.getConditions(), sideTable.getSideAlias());
        List<SqlNode> nodes = new ArrayList<>();
        nodes.addAll(conditionNodes);
        if (whereNode != null){
            nodes.add(whereNode);
        }else{
            SqlBinaryOperator equal
                = new SqlBinaryOperator("=", SqlKind.EQUALS, 30, true, ReturnTypes.BOOLEAN_NULLABLE,
                InferTypes.FIRST_KNOWN, OperandTypes.COMPARABLE_UNORDERED_COMPARABLE_UNORDERED);
            SqlBasicCall andEqual = new SqlBasicCall(equal, SideParser.createEqualNodes(SqlKind.AND), new SqlParserPos(0, 0));
            nodes.add(andEqual);
        }
        SqlBasicCall sqlBasicCall = new SqlBasicCall(and , nodes.toArray(new SqlNode[nodes.size()]), new SqlParserPos(0, 0));
        sqlSelect.setWhere(sqlBasicCall);
        return sqlSelect.toString();
    }

    private List<SqlBasicCall> createConditionNodes(List<String> conditions , Alias alias) {
        SqlBinaryOperator equal
            = new SqlBinaryOperator("=", SqlKind.EQUALS, 30, true, ReturnTypes.BOOLEAN_NULLABLE,
            InferTypes.FIRST_KNOWN, OperandTypes.COMPARABLE_UNORDERED_COMPARABLE_UNORDERED);
        List<SqlBasicCall> nodes = new ArrayList<>(conditions.size());
        int num = 0;
        for(String condition : conditions){
            List<String> fields = new ArrayList<>(2);
            fields.add(alias.getAlias());
            fields.add(condition);
            SqlIdentifier leftIdentifier = new SqlIdentifier(fields, new SqlParserPos(0, 0));
            SqlDynamicParam sqlDynamicParam = new SqlDynamicParam(num++, new SqlParserPos(0, 0));
            SqlNode[] sqlNodes = new SqlNode[2];
            sqlNodes[0] = leftIdentifier;
            sqlNodes[1] = sqlDynamicParam;
            SqlBasicCall andEqual = new SqlBasicCall(equal, sqlNodes , new SqlParserPos(0, 0));
            nodes.add(andEqual);
        }
        return nodes;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        this.sqlClient = createClient(mysqlProperties);
    }

    private SQLClient createClient(MysqlProperties mysqlProperties) {
        JsonObject mysqlClientConfig = new JsonObject();
        mysqlClientConfig.put("url", mysqlProperties.getUrl()).put("driver_class", MYSQL_DRIVER)
            .put("max_pool_size",
                mysqlProperties.getMaxPoolSize() == null ? DEFAULT_MAX_DB_CONN_POOL_SIZE
                    : mysqlProperties.getMaxPoolSize())
            .put("user", mysqlProperties.getUsername()).put("password", mysqlProperties.getPassword());

        VertxOptions vo = new VertxOptions();
        vo.setEventLoopPoolSize(DEFAULT_VERTX_EVENT_LOOP_POOL_SIZE);
        vo.setWorkerPoolSize(DEFAULT_VERTX_WORKER_POOL_SIZE);
        Vertx vertx = Vertx.vertx(vo);
        return JDBCClient.createNonShared(vertx, mysqlClientConfig);
    }

    @Override
    public void asyncInvoke(Row input, ResultFuture<Row> resultFuture) throws Exception {
        JsonArray params = createParams(input, this.getSideTable().getConditionIndexs());
        if (params == null) {
            resultFuture.complete(null);
            return;
        }
        String cacheKey = buildCacheKey(params);
        // query from cache
        Optional<List<JsonObject>> optionalValue = getOrNull(this.getCache(), cacheKey);
        if (optionalValue != null) {
            sendResult(input, resultFuture, optionalValue);
            return;
        }
        // query from db
        sqlClient.getConnection(conn -> {
            if (conn.failed()) {
                // Treatment failures
                resultFuture.completeExceptionally(conn.cause());
                return;
            }
            final SQLConnection connection = conn.result();
            connection.queryWithParams(sql, params, rs -> {
                if (rs.failed()) {
                    LOG.error("Cannot retrieve the data from the database", rs.cause());
                    resultFuture.complete(null);
                    return;
                }
                int resultSize = rs.result().getResults().size();
                Optional<List<JsonObject>> results;
                if (resultSize > 0) {
                    List<JsonObject> cacheContent = rs.result().getRows();
                    results = Optional.of(cacheContent);
                } else {
                    results = Optional.empty();
                }
                setOrNull(this.getCache(), cacheKey, results);
                sendResult(input, resultFuture, results);
                // and close the connection
                connection.close(done -> {
                    if (done.failed()) {
                        throw new RuntimeException(done.cause());
                    }
                });
            });
        });

    }

    private void sendResult(Row input, ResultFuture<Row> resultFuture, Optional<List<JsonObject>> optionalValue) {
        if (!optionalValue.isPresent()) {
            dealMissKey(input, this.getSideTable().getJoinType(), resultFuture);
        } else {
            resultFuture.complete(fillRecord(input, optionalValue.get()));
        }
    }

    private String buildCacheKey(JsonArray params) {
        StringBuilder sb = new StringBuilder();
        for (Object value : params.getList()) {
            sb.append(String.valueOf(value)).append(KEY_SEP);
        }
        return sb.toString();
    }

    private JsonArray createParams(Row input, List<Integer> conditionIndexs) {
        JsonArray params = new JsonArray();
        for (Integer index : conditionIndexs) {
            Object param = input.getField(index);
            if (param == null) {
                LOG.warn("join condition is null ,index:{}", index);
                return null;
            }
            params.add(param);
        }
        return params;
    }

    @Override
    public List<Row> fillRecord(Row input, List<JsonObject> jsonArrays) {
        List<Row> rowList = Lists.newArrayList();
        for (JsonObject value : jsonArrays) {
            Row row = fillRecord(input, value);
            rowList.add(row);
        }
        return rowList;
    }

    private Row fillRecord(Row input, JsonObject value) {
        RowTypeInfo sideTable = this.getSideTable().getSideType();
        int sideSize = sideTable.getArity();
        int inputSize = input.getArity();
        if (this.getSideTable().getRowSize() != (sideSize + inputSize)) {
            LOG.warn("expected row size:{} ,Row:{} , side:{}", this.getSideTable().getRowSize(), input, value);
            throw new IllegalArgumentException("expected row size:" + this.getSideTable().getRowSize()
                + ", but input size:" + inputSize + " and side size:" + sideSize);
        }
        Row row = new Row(this.getSideTable().getRowSize());
        for (int i = 0; i < inputSize; i++) {
            row.setField(i, input.getField(i));
        }
        RowTypeInfo sideType = this.getSideTable().getSideType();
        Map<Integer, String> indexFields = getIndexFields(sideType);
        for (int i = 0; i < sideSize; i++) {
            Object result = value.getValue(indexFields.get(i));
            row.setField(i + inputSize, TransformUtils.transform(result, sideType.getTypeAt(i)));
        }
        return row;
    }

    private Map<Integer, String> getIndexFields(RowTypeInfo sideType) {
        Map<Integer, String> indexFields = new HashMap<>(sideType.getArity());
        String[] fieldNames = sideType.getFieldNames();
        for (String field : fieldNames) {
            indexFields.put(sideType.getFieldIndex(field), field);
        }
        return indexFields;
    }
}
