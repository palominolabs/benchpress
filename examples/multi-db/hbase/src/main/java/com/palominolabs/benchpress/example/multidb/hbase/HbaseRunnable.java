package com.palominolabs.benchpress.example.multidb.hbase;

import com.palominolabs.benchpress.example.multidb.key.KeyGenerator;
import com.palominolabs.benchpress.job.task.TaskOperation;
import com.palominolabs.benchpress.example.multidb.value.ValueGenerator;
import com.palominolabs.benchpress.example.multidb.task.AbstractTaskRunnable;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class HbaseRunnable extends AbstractTaskRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HbaseRunnable.class);

    private final HTable hTable;

    private final byte[] columnFamily;

    private final byte[] qualifier;

    private List<Get> gets;
    private List<Put> puts;

    HbaseRunnable(TaskOperation taskOperation, HTable hTable, byte[] columnFamily, byte[] qualifier,
            KeyGenerator keyGenerator, ValueGenerator valueGenerator, UUID jobId, UUID workerId,
            int sliceId, int numQuanta, int batchSize) {
        super(taskOperation, keyGenerator, workerId, sliceId, numQuanta, batchSize, jobId, valueGenerator);
        this.hTable = hTable;
        this.columnFamily = columnFamily;
        this.qualifier = qualifier;
    }

    @Override
    protected void onBatchStart() {
      switch (getTaskOperation()) {

      case READ:
          gets = new ArrayList<Get>();
          break;

      case WRITE:
          puts = new ArrayList<Put>();
          break;

      default:
          break;

      }
    }

    @Override
    protected void onBatchCompletion() throws IOException {
        switch (getTaskOperation()) {

        case WRITE:
            try {
                // Put operations will be batched according to (cached) region locations and sent
                // in as few RPCs as possible. How many is also a function of the aggregate data
                // size in the puts and the size of the configured write buffer. When the write
                // buffer fills all operations in it will be flushed before further processing.
                hTable.put(puts);
                puts = null;
                hTable.flushCommits();
            } catch (IOException e) {
                logger.warn("Couldn't put", e);
            }
            break;

        case READ:
            try {
                // Get operations will be batched according to (cached) region locations and sent
                // in as few RPCs as possible.
                hTable.get(gets);
                gets = null;
                // TODO: Plug in some kind of verification component here?
            } catch (IOException e) {
                logger.warn("Couldn't put", e);
            }
            break;

        }
    }

    @Override
    protected void onCompletion() throws IOException {
        hTable.close();
    }

    @Override
    protected void onQuanta(byte[] keyBytes, byte[] valueBytes) {
        switch (getTaskOperation()) {

        case WRITE:
            Put put = new Put(keyBytes);
            put.add(columnFamily, qualifier, valueBytes);
            puts.add(put);
            break;

        case READ:
            Get get = new Get(keyBytes);
            get.addColumn(columnFamily, qualifier);
            gets.add(get);
            break;

        }
    }

}
