package com.dfire.platform.alchemy.web.sql;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.InferTypes;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.junit.Test;

/**
 * @author congbai
 * @date 2019/5/21
 */
public class SqlTest {

    SqlParser.Config config = SqlParser.configBuilder().setLex(Lex.MYSQL).build();

    @Test
    public void groupBy() throws SqlParseException {
        String sql = "select name, CASE \n" +
            "\t\t\t\t\t\tWHEN a.url IS NOT NULL\n" +
            "\t\t\t\t\t\tAND a.url <> '-' THEN a.url\n" +
            "\t\t\t\t\t\tELSE '' END AS user_agent_grok" +
            " from test a group By a.name , a.sex";

        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseStmt();
    }

    @Test
    public void select() throws SqlParseException {
        String sql = "select a.name as name , a.age , sex,  a.id , LOG2(a.height) ,a.* from test a ";
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseStmt();
    }

    @Test
    public void where() throws SqlParseException {
        SqlNode node1 = SqlParser.create("select * from test a where name=1 ", config).parseStmt();
//        SqlNode node2 = SqlParser.create("select * from test a where a.name=1 and a.age > 10", config).parseStmt();
        SqlNode node3 = SqlParser.create("select * from test a where (a.name=1 and a.age > 10) or a.height > 170", config).parseStmt();
        SqlBinaryOperator and = new SqlBinaryOperator("AND", SqlKind.AND, 24, true, ReturnTypes.BOOLEAN_NULLABLE_OPTIMIZED, InferTypes.BOOLEAN, OperandTypes.BOOLEAN_BOOLEAN);

        SqlBinaryOperator equal
            = new SqlBinaryOperator("=", SqlKind.EQUALS, 30, true, ReturnTypes.BOOLEAN_NULLABLE,
            InferTypes.FIRST_KNOWN, OperandTypes.COMPARABLE_UNORDERED_COMPARABLE_UNORDERED);
        SqlBasicCall andEqual = new SqlBasicCall(equal, createEqualNodes(), new SqlParserPos(0, 0));
        SqlNode[] nodes = new SqlNode[2];
        nodes[0] = ((SqlSelect)node3).getWhere();
        nodes[1] = andEqual;
        SqlBasicCall sqlBasicCall = new SqlBasicCall(and , nodes, ((SqlSelect)node3).getWhere().getParserPosition());
        ((SqlSelect)node3).setWhere(sqlBasicCall);
    }

    private static SqlNode[] createEqualNodes() {
        SqlNode[] nodes = new SqlNode[2];
        nodes[0] = new SqlIdentifier("a.xxxx",new SqlParserPos(0,0));
        nodes[1] =  new SqlIdentifier("?",new SqlParserPos(0,0));
        return nodes;
    }

    @Test
    public void param() throws SqlParseException {
        String sql = "select a.name as name , a.age , sex,  a.id , LOG2(a.height) ,a.* from test a where a.age = ?";
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseStmt();
    }


}
