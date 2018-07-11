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

    private static final String HBASE_QUORUM = "hbase.zookeeper.quorum";

    private static final String HBASE_ZNODE_PARENT = "zookeeper.znode.parent";
    private final String zookeeper;
    private final String node;
    private final String tableName;
    private final String family;
    private final long bufferSize;
    private final boolean skipWal;
    private org.apache.hadoop.conf.Configuration conf = null;
    private HTable table = null;
    private SerializationSchema<org.apache.flink.types.Row> serializationSchema;

    private String code;

    private HbaseInvoker hbaseInvoker;

    public HBaseOutputFormat(String zookeeper, String node, String tableName, String family, long bufferSize,
        boolean skipWal, SerializationSchema<Row> serializationSchema, String code) {
        this.zookeeper = zookeeper;
        this.node = node;
        this.tableName = tableName;
        this.family = family;
        this.bufferSize = bufferSize;
        this.skipWal=skipWal;
        this.serializationSchema = serializationSchema;
        this.code = code;
    }

    public HBaseOutputFormat(String zookeeper, String node, String tableName, String family, long bufferSize,
        boolean skipWal, SerializationSchema<Row> serializationSchema, HbaseInvoker hbaseInvoker) {
        this.zookeeper = zookeeper;
        this.node = node;
        this.tableName = tableName;
        this.family = family;
        this.bufferSize = bufferSize;
        this.skipWal = skipWal;
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
        if (this.hbaseInvoker == null) {
            this.hbaseInvoker =  GroovyCompiler.create(this.code, RandomUtils.uuid());
        }
    }

    @Override
    public void writeRecord(Tuple2<Boolean, Row> value) throws IOException {
        if(value==null||value.f1==null){
            return;
        }
        Object[] rows = RowUtils.createRows((value.f1));
        Put put = new Put(Bytes.toBytes(hbaseInvoker.getRowKey(rows)));
        put.add(Bytes.toBytes(family), Bytes.toBytes(hbaseInvoker.getQualifier(rows)),
            serializationSchema.serialize(value.f1));
        if(this.skipWal){
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