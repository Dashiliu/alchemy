package com.dfire.platform.alchemy.connectors.tsdb.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dfire.platform.alchemy.api.common.TsdbData;
import com.dfire.platform.alchemy.connectors.tsdb.OpentsdbProperties;
import com.dfire.platform.opentsdb.Builder;
import com.dfire.platform.opentsdb.OpentsdbTemplate;
import com.dfire.platform.opentsdb.put.PutBuilders;

/**
 * @author congbai
 * @date 2018/8/8
 */
public class OpentsdbHandler implements TsdbHandler {

    private transient OpentsdbTemplate opentsdbTemplate;

    public OpentsdbHandler(OpentsdbProperties opentsdbProperties) {
        this.opentsdbTemplate = new OpentsdbTemplate(opentsdbProperties.getOpentsdbUrl());
    }

    @Override
    public void execute(TsdbData tsdbData) {
        opentsdbTemplate
            .execute(getPutBuilder(tsdbData.getMetricValues(), tsdbData.getTags(), tsdbData.getTimestamp()));
    }

    private Builder[] getPutBuilder(Map<String, Number> metricAndValue, Map<String, String> tags, long timestamp) {
        List<Builder> builders = new ArrayList<>(metricAndValue.size());
        metricAndValue.entrySet().forEach((entry) -> {
            PutBuilders.PutBuilder builder
                = PutBuilders.newBuilder().metric(entry.getKey()).timestamp(timestamp).value(entry.getValue());
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                builder.tag(tag.getKey(), tag.getValue());
            }
            builders.add(builder);
        });

        return builders.toArray(new Builder[builders.size()]);
    }

    @Override
    public void close() throws IOException {

    }
}
