package com.palominolabs.benchpress.task.hbase;

import com.palominolabs.benchpress.job.key.KeyGenerator;
import com.palominolabs.benchpress.job.value.ValueGenerator;
import com.palominolabs.benchpress.job.base.task.AbstractTaskRunnable;
import com.palominolabs.benchpress.task.reporting.TaskProgressClient;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

final class HbaseRunnable extends AbstractTaskRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HbaseRunnable.class);

    private final HTable hTable;

    private final byte[] columnFamily;

    private final byte[] qualifier;

    HbaseRunnable(HTable hTable, byte[] columnFamily, byte[] qualifier,
            KeyGenerator keyGenerator, ValueGenerator valueGenerator, TaskProgressClient taskProgressClient,
            AtomicInteger reportSequenceCounter, UUID jobId, UUID workerId,
            int partitionId, int numQuanta, int batchSize, int progressReportQuantaInterval) {
        super(keyGenerator, workerId, partitionId, numQuanta, batchSize, progressReportQuantaInterval,
            taskProgressClient, jobId, valueGenerator, reportSequenceCounter);
        this.hTable = hTable;
        this.columnFamily = columnFamily;
        this.qualifier = qualifier;
    }

    @Override
    protected void onBatchCompletion() throws IOException {
        hTable.flushCommits();
    }

    @Override
    protected void onCompletion() throws IOException {
        hTable.close();
    }

    @Override
    protected void onQuanta(byte[] keyBytes, byte[] valueBytes) {
        // TODO this may flush if it hits the auto flush threshold
        Put put = new Put(keyBytes);
        put.add(columnFamily, qualifier, valueBytes);
        try {
            hTable.put(put);
        } catch (IOException e) {
            logger.warn("Couldn't put", e);
        }
    }

    @Override
    protected void onBatchStart() {
        // no op
    }
}
