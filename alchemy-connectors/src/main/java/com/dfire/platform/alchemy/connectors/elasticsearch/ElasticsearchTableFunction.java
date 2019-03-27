package com.dfire.platform.alchemy.connectors.elasticsearch;

import com.dfire.platform.alchemy.api.util.RandomUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.types.Row;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class ElasticsearchTableFunction implements ElasticsearchSinkFunction<Tuple2<Boolean, Row>>, Serializable {

    private static final long serialVersionUID = 1L;

    private final SimpleDateFormat dateFormat;

    private static final String SPLIT = "-";

    private final String index;

    private final String filedIndex;

    private final JsonRowStringSchema jsonRowSchema;

    public ElasticsearchTableFunction(String index, JsonRowStringSchema jsonRowSchema, String filedIndex, String formatDate) {
        this.index = index;
        this.jsonRowSchema = jsonRowSchema;
        this.filedIndex = filedIndex;
        if (StringUtils.isNotBlank(formatDate)){
            this.dateFormat = new SimpleDateFormat(formatDate);
        }else {
            this.dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        }

    }

    @Override
    public void process(Tuple2<Boolean, Row> booleanRowTuple2, RuntimeContext runtimeContext,
                        RequestIndexer requestIndexer) {
        if (booleanRowTuple2 == null || booleanRowTuple2.f1 == null) {
            return;
        }
        if (booleanRowTuple2.f0) {
            requestIndexer.add(createIndexRequest(booleanRowTuple2.f1));
        }
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
        if (StringUtils.isBlank(filedIndex)) {
            return this.index;
        }

        int arrayIndex = 0;
        boolean result = false;
        for (int i = 0; i < this.jsonRowSchema.getFieldNames().length; i++) {
            if (filedIndex.equals(this.jsonRowSchema.getFieldNames()[i])) {
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