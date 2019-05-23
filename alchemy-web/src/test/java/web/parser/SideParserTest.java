package web.parser;

import com.dfire.platform.alchemy.web.common.Alias;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.InferTypes;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.junit.Test;

import java.util.*;

/**
 * @author congbai
 * @date 2019/5/17
 */
public class SideParserTest {

    @Test
    public void parse() throws SqlParseException {
        String sideTableName = "s";
        String sql
            = "insert into test_table  select t.* ,s.* from (select p.*,a.name from people  p join adress as a on p.id = a.p_id )as t join s  on t.name = s.name where (t.sex = 1 or s.age > 10) and t.height > 170";
        SqlParser.Config config = SqlParser.configBuilder().setLex(Lex.MYSQL).build();
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseStmt();
        Deque<SqlNode> deque = new ArrayDeque<>();
        parse(sqlNode, deque);
        SqlNode last;
        SqlSelect modifyNode = null;
        while ((last = deque.pollLast()) != null) {
            if (modifyNode != null){
                modify(last,modifyNode);
                modifyNode = null;
            }
            if (last.getKind() == SqlKind.SELECT) {
                SqlSelect sqlSelect = (SqlSelect) last;
                SqlNode node = sqlSelect.getFrom();
                if (SqlKind.JOIN != node.getKind()){
                    continue;
                }
                SqlJoin sqlJoin = (SqlJoin) node;
                Alias sideAlias = getTableName(sqlJoin.getRight());
                if(!sideTableName.equalsIgnoreCase(sideAlias.getTable())){
                    continue;
                }
                Alias leftAlias = getTableName(sqlJoin.getLeft());
                SqlSelect leftSelect = newSelect(sqlSelect,leftAlias.getTable(),leftAlias.getAlias(),true ,false);
                SqlSelect rightSelect = newSelect(sqlSelect,sideAlias.getTable(),sideAlias.getAlias(),false ,false);
                SqlSelect newSelect = newSelect(sqlSelect,leftAlias.getTable()+"_"+sideAlias.getTable(),leftAlias.getAlias()+"_"+sideAlias.getAlias(),false ,true);
                System.out.println("**********************");
                System.out.println(leftSelect.toString());
                System.out.println("**********************");
                System.out.println(rightSelect.toString());
                System.out.println("**********************");
                System.out.println(newSelect.toString());
                System.out.println("**********************");
                modifyNode = newSelect;
            }
            System.out.println(last.toString());
            System.out.println("---------------------");
        }
    }

    private Alias getTableName(SqlNode right) {
        SqlKind sqlKind = right.getKind();
        Alias alias;
        switch (sqlKind) {
            case IDENTIFIER:
                    SqlIdentifier sqlIdentifier = (SqlIdentifier)right;
                    alias = new Alias(sqlIdentifier.names.get(0),sqlIdentifier.names.get(0));
                    break;
            case AS:
                SqlBasicCall sqlBasicCall = (SqlBasicCall)right;
                SqlNode first = sqlBasicCall.getOperands()[0];
                SqlNode second = sqlBasicCall.getOperands()[1];
                if (first.getKind() == SqlKind.IDENTIFIER){
                    alias = new Alias(((SqlIdentifier)first).names.get(0),((SqlIdentifier)second).names.get(0));
                }else{
                    alias = new Alias(((SqlIdentifier)second).names.get(0),((SqlIdentifier)second).names.get(0));
                }
                break;
            default:
                throw new UnsupportedOperationException("暂时不支持"+sqlKind);
        }
        return alias;
    }

    private void modify(SqlNode sqlNode, SqlSelect sqlSelect) {
        SqlKind sqlKind = sqlNode.getKind();
        switch (sqlKind) {
            case INSERT:
                SqlInsert sqlInsert = ((SqlInsert)sqlNode);
                sqlInsert.setSource(sqlSelect);
                break;
            case SELECT:
                SqlSelect select = (SqlSelect)sqlNode;
                select.setFrom(sqlSelect);
                break;
            case AS:
                SqlBasicCall basicCall =(SqlBasicCall)sqlNode;
                basicCall.setOperand(0,sqlSelect);
                break;
            default:
                throw new UnsupportedOperationException(sqlKind+"目前不支持维表操作");
        }
    }

    private void parse(SqlNode sqlNode, Deque<SqlNode> deque) {
        deque.offer(sqlNode);
        SqlKind sqlKind = sqlNode.getKind();
        switch (sqlKind) {
            case INSERT:
                SqlNode sqlSource = ((SqlInsert)sqlNode).getSource();
                parse(sqlSource, deque);
                break;
            case SELECT:
                SqlNode sqlFrom = ((SqlSelect)sqlNode).getFrom();
                parse(sqlFrom, deque);
                break;
            case JOIN:
                SqlNode sqlLeft = ((SqlJoin)sqlNode).getLeft();
                SqlNode sqlRight = ((SqlJoin)sqlNode).getRight();
                parse(sqlLeft, deque);
                parse(sqlRight, deque);
                break;
            case AS:
                SqlNode sqlAs = ((SqlBasicCall)sqlNode).getOperands()[0];
                parse(sqlAs, deque);
                break;
            default:
                return;
        }
    }

    @Test
    public void change() throws SqlParseException {
        String sql
            = "insert into MyTable select DISTINCT a.id ,a.age as age1 ,UPPER(a.name) as name,CASE WHEN a.url IS NOT NULL AND a.url <> '-' THEN a.url ELSE '' END AS user_agent_grok,  b.*  from people a join address b on a.id = b.id WHERE a.age > 10 group by a.name";
        SqlParser.Config config = SqlParser.configBuilder().setLex(Lex.MYSQL).build();
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseStmt();
        // Update from node
        SqlOperator operator = new SqlAsOperator();
        SqlParserPos sqlParserPos = new SqlParserPos(0, 0);
        // String joinLeftTableName = joinInfo.getLeftTableName();
        // String joinLeftTableAlias = joinInfo.getLeftTableAlias();
        // joinLeftTableName = Strings.isNullOrEmpty(joinLeftTableName) ? joinLeftTableAlias : joinLeftTableName;
        String newTableName = "new_table";
        String newTableAlias = "n";
        SqlIdentifier sqlIdentifier = new SqlIdentifier(newTableName, null, sqlParserPos);
        SqlIdentifier sqlIdentifierAlias = new SqlIdentifier(newTableAlias, null, sqlParserPos);
        SqlNode[] sqlNodes = new SqlNode[2];
        sqlNodes[0] = sqlIdentifier;
        sqlNodes[1] = sqlIdentifierAlias;
        SqlBasicCall sqlBasicCall = new SqlBasicCall(operator, sqlNodes, sqlParserPos);
        SqlInsert sqlInsert = (SqlInsert)sqlNode;
        SqlSelect select = (SqlSelect)sqlInsert.getSource();
        //第一步，替换*
        SqlSelect left = newSelect(select,"people" ,"a" ,true, false);
        SqlSelect right = newSelect(select,"address" ,"b" ,false, false);
        SqlSelect newTableSelect = newSelect(select,"new_table" ,"n" ,false, true);
        // SqlNodeList sqlNodeList =select.getSelectList();
        // SqlIdentifier sqlIdentifier1 = (SqlIdentifier) sqlNodeList.get(0);
        // sqlIdentifier1=sqlIdentifier1.setName(0,newTableName);
        // SqlSelect sqlSelect =new SqlSelect(
        // select.getParserPosition(),
        // (SqlNodeList) select.getOperandList().get(0),
        // select.getSelectList(),
        // sqlBasicCall,
        // select.getWhere(),
        // select.getGroup(),
        // select.getHaving(),
        // select.getWindowList(),
        // select.getOrderList(),
        // select.getOffset(),
        // select.getFetch());
        //// sqlSelect.getOperandList()
        // sqlInsert.setSource(sqlSelect);
        // sqlNode(sqlBasicCall);

    }

    private SqlSelect newSelect(SqlSelect selectSelf, String table, String alias, boolean left, boolean newTable) {
        List<SqlNode> operand = selectSelf.getOperandList();
        SqlNodeList keywordList = (SqlNodeList)operand.get(0);
        SqlNodeList selectList = (SqlNodeList)operand.get(1);
        SqlNode from = operand.get(2);
        SqlNode where = operand.get(3);
        SqlNodeList groupBy = (SqlNodeList)operand.get(4);
        SqlNode having = operand.get(5);
        SqlNodeList windowDecls = (SqlNodeList)operand.get(6);
        SqlNodeList orderBy = (SqlNodeList)operand.get(7);
        SqlNode offset = operand.get(8);
        SqlNode fetch = operand.get(9);
        if (left) {
            return newSelect(selectSelf.getParserPosition(), keywordList, selectList,((SqlJoin)from).getLeft(), where, groupBy, having,
                windowDecls, orderBy, offset, fetch, table, alias, newTable);
        }if(newTable){
            SqlIdentifier identifierFirst =new SqlIdentifier(table , from.getParserPosition());
            SqlIdentifier identifierSecond =new SqlIdentifier(alias , from.getParserPosition());
            return newSelect(selectSelf.getParserPosition(), null, selectList, new SqlBasicCall(new SqlAsOperator(),new SqlNode[]{identifierFirst,identifierSecond},from.getParserPosition()) , where, groupBy, having,
                windowDecls, orderBy, offset, fetch, table, alias, newTable);
        } else {
            return newSelect(selectSelf.getParserPosition(), null, selectList, ((SqlJoin)from).getRight(), where, groupBy, having,
                windowDecls, orderBy, offset, fetch, table, alias, newTable);
        }

    }

    private SqlSelect newSelect(SqlParserPos parserPosition, SqlNodeList keywordList, SqlNodeList selectList,
        SqlNode fromNode, SqlNode whereNode, SqlNodeList groupByNode, SqlNode havingNode, SqlNodeList windowDeclsList,
        SqlNodeList orderByList, SqlNode offsetNode, SqlNode fetchNode, String tableName, String alias,
        boolean newTable) {
        SqlNodeList keyword = keywordList == null ? null : keywordList;
        SqlNodeList select = selectList == null ? null : reduce(selectList, tableName, alias, newTable);
        SqlNode from = fromNode == null ? null : fromNode;
        SqlNode where = whereNode == null ? null : reduce(whereNode, tableName, alias, newTable);
        SqlNodeList groupBy = groupByNode == null ? null : reduce(groupByNode, tableName, alias, newTable);
        SqlNode having = havingNode == null ? null : reduce(havingNode, tableName, alias, newTable);
        SqlNodeList windowDecls = windowDeclsList == null ? null : reduce(windowDeclsList, tableName, alias, newTable);
        SqlNodeList orderBy = orderByList == null ? null : reduce(orderByList, tableName, alias, newTable);
        SqlNode offset = offsetNode == null ? null : reduce(offsetNode, tableName, alias, newTable);
        SqlNode fetch = fetchNode == null ? null : reduce(fetchNode, tableName, alias, newTable);
        return new SqlSelect(parserPosition, keyword, select, from, where, groupBy, having, windowDecls, orderBy,
            offset, fetch);
    }

    private SqlNode reduce(SqlNode sqlNode, String table, String alias, boolean newTable) {
        if (sqlNode == null) {
            return null;
        }
        SqlNode node = sqlNode.clone(sqlNode.getParserPosition());
        return changeTableName(node, table, alias, newTable);
    }

    private SqlNodeList reduce(SqlNodeList sqlNodes, String table, String alias, boolean newTable) {
        if (sqlNodes == null) {
            return sqlNodes;
        }
        SqlNodeList nodes = sqlNodes.clone(new SqlParserPos(0, 0));
        List<SqlNode> newNodes = new ArrayList<>(nodes.size());
        Iterator<SqlNode> sqlNodeIterable = nodes.iterator();
        while (sqlNodeIterable.hasNext()) {
            SqlNode sqlNode = sqlNodeIterable.next();
            sqlNode = changeTableName(sqlNode, table, alias, newTable);
            if (sqlNode != null) {
                newNodes.add(sqlNode);
            }
        }
        if (newNodes.size() > 0){
            return new SqlNodeList(newNodes,nodes.getParserPosition());
        }else{
            return null;
        }
    }

    public SqlNode changeTableName(SqlNode sqlNode, String table, String alias, boolean newTable) {
        SqlKind sqlKind = sqlNode.getKind();
        String tableName;
        switch (sqlKind) {
            case IDENTIFIER:
                if (newTable) {
                    SqlIdentifier sqlIdentifier = new SqlIdentifier(new ArrayList<>(((SqlIdentifier)sqlNode).names.asList()),sqlNode.getParserPosition());
                    return sqlIdentifier.setName(0, alias);
                } else {
                    SqlIdentifier sqlIdentifier = (SqlIdentifier)sqlNode;
                    tableName = sqlIdentifier.names.get(0);
                    if (tableName.equalsIgnoreCase(alias)) {
                        return sqlIdentifier;
                    } else {
                        return null;
                    }
                }
            case OR:
            case AND:
                SqlBasicCall call = (SqlBasicCall)sqlNode;
                SqlNode[] nodes = call.getOperands();
                List<SqlNode> sqlNodeList = new ArrayList<>(nodes.length);
                for(int i = 0 ; i < nodes.length ; i ++){
                    SqlNode node = changeTableName(nodes[i],table,alias,newTable);
                    if (node != null){
                        sqlNodeList.add(node);
                    }
                }
                if(sqlNodeList.size() == 1){
                    SqlBinaryOperator equal = new SqlBinaryOperator("=", SqlKind.EQUALS, 30, true, ReturnTypes.BOOLEAN_NULLABLE, InferTypes.FIRST_KNOWN, OperandTypes.COMPARABLE_UNORDERED_COMPARABLE_UNORDERED);
                    SqlBasicCall andEqual = new SqlBasicCall(equal,createEqualNodes(sqlKind) , new SqlParserPos(0,0));
                    sqlNodeList.add(andEqual);
                    return call.getOperator().createCall(call.getFunctionQuantifier(),call.getParserPosition(),
                        sqlNodeList.toArray(new SqlNode[sqlNodeList.size()]));
                }else if(sqlNodeList.size() > 1){
                    return call.getOperator().createCall(call.getFunctionQuantifier(),call.getParserPosition(),
                        sqlNodeList.toArray(new SqlNode[sqlNodeList.size()]));
                }else{
                    return  null;
                }

            default:
                if (sqlNode instanceof SqlBasicCall) {
                    SqlBasicCall sqlBasicCall = (SqlBasicCall)sqlNode;
                    SqlNode node = changeTableName(sqlBasicCall.getOperands()[0], table, alias, newTable);
                    if (node == null) {
                        return null;
                    } else {
                        SqlBasicCall basicCall = (SqlBasicCall) sqlBasicCall.getOperator().createCall(sqlBasicCall.getFunctionQuantifier(),sqlBasicCall.getParserPosition(),Arrays.copyOf(sqlBasicCall.getOperands(),sqlBasicCall.getOperands().length));
                        basicCall.setOperand(0, node);
                        return basicCall;
                    }
                } else {
                    throw new UnsupportedOperationException("don't support " +sqlNode);
                }
        }
    }

    private SqlNode[] createEqualNodes(SqlKind sqlKind) {
        SqlNode[] nodes =  new SqlNode[2];
        if (SqlKind.AND == sqlKind){
            nodes[0] = SqlLiteral.createExactNumeric("1",new SqlParserPos(0,0));
            nodes[1] = SqlLiteral.createExactNumeric("1",new SqlParserPos(0,0));
        }else{
            nodes[0] = SqlLiteral.createExactNumeric("0",new SqlParserPos(0,0));
            nodes[1] = SqlLiteral.createExactNumeric("1",new SqlParserPos(0,0));
        }
        return  nodes;
    }

}
