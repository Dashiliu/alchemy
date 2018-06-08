package com.dfire.platform.connectors.hbase;

import java.io.IOException;

import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.types.Row;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.dfire.platform.api.sink.HbaseInvoker;

public class HBaseOutputFormat implements OutputFormat<Tuple2<Boolean, Row>> {

    private static final long serialVersionUID = 1L;

    private static final String HBASE_QUORUM = "hbase.zookeeper.quorum";

    private static final String HBASE_ZNODE_PARENT = "zookeeper.znode.parent";
    private final String zookeeper;
    private final String node;
    private final String tableName;
    private final String family;
    private final long bufferSize;
    private final int rowLength;
    private org.apache.hadoop.conf.Configuration conf = null;
    private HTable table = null;
    private SerializationSchema<org.apache.flink.types.Row> serializationSchema;

    private HbaseInvoker hbaseInvoker;

    public HBaseOutputFormat(String zookeeper, String node, String tableName, String family, long bufferSize,
        int rowLength, SerializationSchema<Row> serializationSchema, HbaseInvoker hbaseInvoker) {
        this.zookeeper = zookeeper;
        this.node = node;
        this.tableName = tableName;
        this.family = family;
        this.bufferSize = bufferSize;
        this.rowLength = rowLength;
        this.serializationSchema = serializationSchema;
        this.hbaseInvoker = hbaseInvoker;
    }

    @Override
    public void configure(Configuration parameters) {
        conf = HBaseConfiguration.create();
        conf.set(HBASE_QUORUM, zookeeper);
        conf.set(HBASE_ZNODE_PARENT, node);
    }

    @Override
    public void open(int taskNumber, int numTasks) throws IOException {
        table = new HTable(conf, tableName);
        if (bufferSize > 0) {
            table.setAutoFlushTo(false);
            table.setWriteBufferSize(bufferSize);
        }
    }

    @Override
    public void writeRecord(Tuple2<Boolean, Row> value) throws IOException {
        Object[] rows = createRows(value);
        Put put = new Put(Bytes.toBytes(hbaseInvoker.getRowKey(rows)));
        put.add(Bytes.toBytes(family), Bytes.toBytes(hbaseInvoker.getQualifier(rows)),
            serializationSchema.serialize(value.f1));
        table.put(put);
    }

    private Object[] createRows(Tuple2<Boolean, Row> value) {
        Object[] rows = new Object[rowLength];
        for (int i = 0; i < rowLength; i++) {
            rows[i] = value.f1.getField(i);
        }
        return rows;
    }

    @Override
    public void close() throws IOException {
        table.flushCommits();
        table.close();
    }

}