package com.dfire.platform.alchemy.connectors.hbase;

import java.io.IOException;

import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.types.Row;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.dfire.platform.alchemy.api.sink.HbaseInvoker;
import com.dfire.platform.alchemy.api.util.GroovyCompiler;
import com.dfire.platform.alchemy.api.util.RandomUtils;
import com.dfire.platform.alchemy.api.util.RowUtils;

/**
 * @author dongbinglin
 */
public class HBaseOutputFormat implements OutputFormat<Tuple2<Boolean, Row>> {

    private static final long serialVersionUID = 1L;

    private static final String HBASE_QUORUM = "hbase.zookeeper.quorum";

    private static final String HBASE_ZNODE_PARENT = "zookeeper.znode.parent";
    private final HbaseProperties hbaseProperties;
    private org.apache.hadoop.conf.Configuration conf = null;
    private HTable table = null;
    private SerializationSchema<org.apache.flink.types.Row> serializationSchema;

    private String code;

    private HbaseInvoker hbaseInvoker;

    public HBaseOutputFormat(HbaseProperties hbaseProperties, SerializationSchema<Row> serializationSchema,
        String code) {
        this.hbaseProperties = hbaseProperties;
        this.serializationSchema = serializationSchema;
        this.code = code;
    }

    @Override
    public void configure(Configuration parameters) {
        conf = HBaseConfiguration.create();
        conf.set(HBASE_QUORUM, this.hbaseProperties.getZookeeper());
        conf.set(HBASE_ZNODE_PARENT, this.hbaseProperties.getNode());
    }

    @Override
    public void open(int taskNumber, int numTasks) throws IOException {
        table = new HTable(conf, this.hbaseProperties.getTableName());
        if (this.hbaseProperties.getBufferSize() > 0) {
            table.setAutoFlushTo(false);
            table.setWriteBufferSize(this.hbaseProperties.getBufferSize());
        }
        try {
            Class clazz = Class.forName(this.code);
            this.hbaseInvoker = (HbaseInvoker)clazz.newInstance();
        } catch (Exception e) {
            this.hbaseInvoker = GroovyCompiler.create(this.code, RandomUtils.uuid());
        }
    }

    @Override
    public void writeRecord(Tuple2<Boolean, Row> value) throws IOException {
        if (value == null || value.f1 == null) {
            return;
        }
        Object[] rows = RowUtils.createRows((value.f1));
        Put put = new Put(Bytes.toBytes(hbaseInvoker.getRowKey(rows)));
        put.add(Bytes.toBytes(this.hbaseInvoker.getFamily(rows)), Bytes.toBytes(hbaseInvoker.getQualifier(rows)),
            serializationSchema.serialize(value.f1));
        if (this.hbaseProperties.isSkipWal()) {
            put.setDurability(Durability.SKIP_WAL);
        }
        table.put(put);
    }

    @Override
    public void close() throws IOException {
        table.flushCommits();
        table.close();
    }

}