package com.dfire.platform.alchemy.connectors.elasticsearch;

import com.dfire.platform.alchemy.api.util.RandomUtils;
import com.dfire.platform.alchemy.connectors.common.MetricFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.formats.json.JsonRowSerializationSchema;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.table.shaded.org.apache.commons.lang3.StringUtils;
import org.apache.flink.types.Row;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class ElasticsearchTableFunction implements MetricFunction,  ElasticsearchSinkFunction<Row>, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchTableFunction.class);

    private static final long serialVersionUID = 1L;

    private static final String ELASTICSEARCH_METRICS_GROUP = "Elasticsearch";

    private final SimpleDateFormat dateFormat;

    private static final String SPLIT = "-";

    private final String index;

    private final Integer fieldIndex;

    private final JsonRowSerializationSchema jsonRowSchema;

    private MapFunction<byte[], byte[]> mapFunction;

    private Counter numRecordsOut;


    public ElasticsearchTableFunction(String index,
                                      Integer fieldIndex,
                                      String formatDate,
                                      JsonRowSerializationSchema jsonRowSchema,
                                      MapFunction<byte[], byte[]> mapFunction) {
        this.index = index;
        this.jsonRowSchema = jsonRowSchema;
        this.fieldIndex = fieldIndex;
        if (StringUtils.isNotBlank(formatDate)){
            this.dateFormat = new SimpleDateFormat(formatDate);
        }else {
            this.dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        }
        this.mapFunction = mapFunction;
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
        byte[] source = this.jsonRowSchema.serialize(row);
        if(mapFunction != null){
            try {
                source = mapFunction.map(source);
            } catch (Exception e) {
                logger.error("Map Failed", e);
            }
        }
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
        if (fieldIndex == null) {
            return this.index;
        }
        return (String) row.getField(fieldIndex);
    }

    @Override
    public String metricGroupName() {
        return ELASTICSEARCH_METRICS_GROUP;
    }
}