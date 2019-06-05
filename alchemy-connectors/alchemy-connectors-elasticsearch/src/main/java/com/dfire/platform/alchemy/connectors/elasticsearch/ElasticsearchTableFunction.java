package com.dfire.platform.alchemy.connectors.elasticsearch;

import com.dfire.platform.alchemy.api.util.RandomUtils;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.table.shaded.org.apache.commons.lang3.StringUtils;
import org.apache.flink.types.Row;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class ElasticsearchTableFunction implements ElasticsearchSinkFunction<Row>, Serializable {

    private static final long serialVersionUID = 1L;

    private final SimpleDateFormat dateFormat;

    private static final String SPLIT = "-";

    private final String index;

    private final String fieldIndex;

    private final JsonRowStringSchema jsonRowSchema;

    public ElasticsearchTableFunction(String index, String fieldIndex, String formatDate, JsonRowStringSchema jsonRowSchema) {
        this.index = index;
        this.jsonRowSchema = jsonRowSchema;
        this.fieldIndex = fieldIndex;
        if (StringUtils.isNotBlank(formatDate)){
            this.dateFormat = new SimpleDateFormat(formatDate);
        }else {
            this.dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        }
    }

    @Override
    public void process(Row row, RuntimeContext runtimeContext,
                        RequestIndexer requestIndexer) {
        if (row == null ) {
            return;
        }
        requestIndexer.add(createIndexRequest(row));
    }

    private IndexRequest createIndexRequest(Row row) {
        String source = this.jsonRowSchema.serialize(row);

        return Requests.indexRequest().index(getIndex(row) + SPLIT + dateFormat.format(System.currentTimeMillis()))
                .id(RandomUtils.uuid()).type("*").source(source);
    }


    /**
     * 获取自定义索引名
     *
     * @param row
     * @return
     */
    private String getIndex(Row row) {
        if (StringUtils.isBlank(fieldIndex)) {
            return this.index;
        }

        int arrayIndex = 0;
        boolean result = false;
        for (int i = 0; i < this.jsonRowSchema.getFieldNames().length; i++) {
            if (fieldIndex.equals(this.jsonRowSchema.getFieldNames()[i])) {
                arrayIndex = i;
                result = true;
                break;
            }
        }
        if (result) {
            return (String) row.getField(arrayIndex);
        }
        return this.index;
    }

}