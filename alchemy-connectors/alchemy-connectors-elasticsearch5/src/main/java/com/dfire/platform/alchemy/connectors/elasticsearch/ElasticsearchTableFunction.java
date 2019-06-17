package com.dfire.platform.alchemy.connectors.elasticsearch;

import com.dfire.platform.alchemy.api.util.RandomUtils;
import com.dfire.platform.alchemy.connectors.common.MetricFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.table.shaded.org.apache.commons.lang3.StringUtils;
import org.apache.flink.types.Row;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class ElasticsearchTableFunction implements MetricFunction,  ElasticsearchSinkFunction<Row>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final String ELASTICSEARCH_METRICS_GROUP = "Elasticsearch";

    private final SimpleDateFormat dateFormat;

    private static final String SPLIT = "-";

    private final String index;

    private final String indexField;

    private final JsonRowStringSchema jsonRowSchema;

    private final String[] fieldNames;

    private Counter numRecordsOut;


    public ElasticsearchTableFunction(String index, String indexField, String formatDate, JsonRowStringSchema jsonRowSchema, String[] fieldNames) {
        this.index = index;
        this.jsonRowSchema = jsonRowSchema;
        this.indexField = indexField;
        if (StringUtils.isNotBlank(formatDate)){
            this.dateFormat = new SimpleDateFormat(formatDate);
        }else {
            this.dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        }
        this.fieldNames = fieldNames;
    }

    @Override
    public void process(Row row, RuntimeContext runtimeContext,
                        RequestIndexer requestIndexer) {
        if (row == null ) {
            return;
        }
        requestIndexer.add(createIndexRequest(row));
        numRecordsOut = createOrGet(numRecordsOut, runtimeContext);
        numRecordsOut.inc();
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
        if (StringUtils.isBlank(indexField)) {
            return this.index;
        }
        for (int i = 0; i < this.fieldNames.length; i++) {
            if (indexField.equals(this.fieldNames[i])) {
                return (String) row.getField(i);
            }
        }
        return this.index;
    }

    @Override
    public String metricGroupName() {
        return ELASTICSEARCH_METRICS_GROUP;
    }
}