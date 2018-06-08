package com.dfire.platform.connectors.elasticsearch;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.types.Row;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import com.dfire.platform.api.util.RandomUtils;

public class ElasticsearchTableFunction implements ElasticsearchSinkFunction<Tuple2<Boolean, Row>>, Serializable {

    private static final SimpleDateFormat FORMAT_INDEX = new SimpleDateFormat("yyyy.MM.dd");

    private static final String SPLIT = "-";

    private final String index;

    private final JsonRowStringSchema jsonRowSchema;

    public ElasticsearchTableFunction(String index, JsonRowStringSchema jsonRowSchema) {
        this.index = index;
        this.jsonRowSchema = jsonRowSchema;
    }

    @Override
    public void process(Tuple2<Boolean, Row> booleanRowTuple2, RuntimeContext runtimeContext,
        RequestIndexer requestIndexer) {
        if (booleanRowTuple2.f0) {
            requestIndexer.add(createIndexRequest(booleanRowTuple2.f1));
        }
    }

    private IndexRequest createIndexRequest(Row row) {
        return Requests.indexRequest().index(this.index + SPLIT + FORMAT_INDEX.format(System.currentTimeMillis()))
            .id(RandomUtils.uuid()).type("*").source(this.jsonRowSchema.serialize(row));
    }

}