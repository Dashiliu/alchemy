package com.dfire.platform.alchemy.web.side;

import com.dfire.platform.alchemy.web.common.Alias;
import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Field;
import com.dfire.platform.alchemy.web.common.Side;
import com.dfire.platform.alchemy.web.descriptor.SourceDescriptor;
import org.apache.calcite.sql.*;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.typeutils.TypeStringUtils;
import org.apache.flink.types.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author congbai
 * @date 2019/5/21
 */
public class SideStream {


    public static DataStream<Row> buildStream(StreamTableEnvironment env, SqlSelect sqlSelect, Alias leftAlias, Alias sideAlias, SourceDescriptor sideSource) throws Exception {
        SqlSelect leftSelect = SideParser.newSelect(sqlSelect,leftAlias.getTable(),leftAlias.getAlias(),true ,false);
        //register leftTable
        Table leftTable =env.sqlQuery(leftSelect.toString());
        DataStream<Row> leftStream = env.toAppendStream(leftTable , Row.class);
        SqlSelect rightSelect = SideParser.newSelect(sqlSelect,sideAlias.getTable(),sideAlias.getAlias(),false ,false);
        SqlJoin sqlJoin = (SqlJoin) sqlSelect.getFrom();
        List<String> equalFields = SideParser.findConditionFields(sqlJoin.getCondition() , leftAlias.getAlias());
        if(sideSource.getSide().isPartition()){
            leftStream = leftStream.keyBy(equalFields.toArray(new String[equalFields.size()]));
        }
        RowTypeInfo sideType = createSideType(rightSelect.getSelectList() , sideSource.getSchema());
        RowTypeInfo returnType = createReturnType(leftStream.getType(),sideType);
        SideTableInfo sideTable = createSideTable(returnType, sideType, sqlJoin.getJoinType(),  rightSelect, equalFields , sideAlias, sideSource.getSide());
        DataStream<Row> returnStream;
        if(sideSource.getSide().isAsync()){
            AsyncReqRow reqRow =ReqRowFactory.getAsync(ClusterType.FLINK, sideTable, sideSource);
            returnStream = AsyncDataStream.orderedWait(leftStream, reqRow, sideSource.getSide().getTimeout(), TimeUnit.MILLISECONDS, sideSource.getSide().getCapacity());
        }else{
            SyncReqRow syncReqRow = ReqRowFactory.getSync(ClusterType.FLINK , sideTable, sideSource);
            returnStream = leftStream.flatMap(syncReqRow);
        }
        returnStream.getTransformation().setOutputType(returnType);
        return returnStream;
    }

    private static SideTableInfo createSideTable(RowTypeInfo leftType, RowTypeInfo sideType, JoinType joinType, SqlSelect rightSelect, List<String> equalFields, Alias sideAlias, Side side) {
        List<Integer> indexFields = createFieldIndex(leftType, equalFields);
        SideTableInfo sideTable = new SideTableInfo();
        sideTable.setConditionIndexs(indexFields);
        sideTable.setConditions(equalFields);
        sideTable.setSide(side);
        sideTable.setJoinType(joinType);
        sideTable.setRowSize(leftType.getArity() + sideType.getArity());
        sideTable.setSideAlias(sideAlias);
        sideTable.setSideType(sideType);
        sideTable.setSql(rightSelect.toString());
        return sideTable;
    }

    private static List<Integer> createFieldIndex(RowTypeInfo leftType, List<String> equalFields) {
        List<Integer> indexFields = new ArrayList<>(equalFields.size());
        String[] names = leftType.getFieldNames();
        for(String field : equalFields){
            for(int i = 0 ; i < names.length ; i++){
                if (field.equalsIgnoreCase(names[i])){
                    indexFields.add(i);
                    break;
                }
            }
        }
        return indexFields;
    }

    private static RowTypeInfo createReturnType(TypeInformation<Row> type, RowTypeInfo sideType) {
        RowTypeInfo leftTypeInfo = (RowTypeInfo) type;
        int leftArity = type.getArity();
        int rightArity = sideType.getArity();
        int size = leftArity + rightArity;
        String[] columnNames = new String[size];
        TypeInformation[] columnTypes = new TypeInformation[size];
        String[] fileNames = leftTypeInfo.getFieldNames();
        for (int i =0 ; i < leftArity ; i++){
            columnNames[i] = fileNames[i];
            columnTypes[i] = leftTypeInfo.getTypeAt(i);
        }
        for(int i = 0 ; i < rightArity ; i++){
            columnNames[leftArity + i] = sideType.getFieldNames()[i];
            columnTypes[leftArity + i] = sideType.getTypeAt(i);
        }

        return new RowTypeInfo(columnTypes , columnNames);
    }

    private static RowTypeInfo createSideType(SqlNodeList selectList, List<Field> fields){
        List<String> selectField = SideParser.findSelectField(selectList);
        Map<String , TypeInformation> selectTypes = createTypes(selectField , fields);
        int selectSize = selectField.size();
        String[] columnNames = new String[selectSize];
        TypeInformation[] columnTypes = new TypeInformation[selectSize];
        for(int i = 0 ; i < selectSize ; i++){
            columnNames[i] = selectField.get(i);
            columnTypes[i] = selectTypes.get(selectField.get(i));
        }
        return new RowTypeInfo(columnTypes, columnNames);
    }

    private static Map<String, TypeInformation> createTypes(List<String> selectField, List<Field> fields) {
        int size;
        boolean all = false;
        if (selectField.size() == 0){
            size = fields.size();
            all = true;
        }else{
            size = selectField.size();
        }
        Map<String , TypeInformation> types = new HashMap<>(size);
        for (int i =0 ; i < size ; i ++){
            if (all){
                types.put(fields.get(i).getName() ,  TypeStringUtils.readTypeInfo(fields.get(i).getType()));
            }else{
                if (selectField.contains(fields.get(i).getName())){
                    types.put(fields.get(i).getName() ,  TypeStringUtils.readTypeInfo(fields.get(i).getType()));
                }
            }
        }
        return types;
    }
}
