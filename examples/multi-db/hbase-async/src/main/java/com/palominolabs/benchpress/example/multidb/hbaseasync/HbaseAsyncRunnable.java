package com.palominolabs.benchpress.example.multidb.hbaseasync;

import com.palominolabs.benchpress.example.multidb.key.KeyGenerator;
import com.palominolabs.benchpress.job.task.TaskOperation;
import com.palominolabs.benchpress.example.multidb.value.ValueGenerator;
import com.palominolabs.benchpress.example.multidb.task.AbstractTaskRunnable;
import com.stumbleupon.async.Deferred;
import org.hbase.async.HBaseClient;
import org.hbase.async.PutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

import static com.google.common.base.Charsets.UTF_8;

final class HbaseAsyncRunnable extends AbstractTaskRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HbaseAsyncRunnable.class);

    private final HBaseClient client;
    private final byte[] table;
    private final byte[] columnFamily;
    private final byte[] qualifier;

    public HbaseAsyncRunnable(TaskOperation taskOperation, HBaseClient client, int numQuanta, String table,
            String columnFamily, String qualifier, KeyGenerator keyGenerator, ValueGenerator valueGenerator,
            UUID workerId, int partitionId, int batchSize, UUID jobId) {
        super(taskOperation, keyGenerator, workerId, partitionId, numQuanta, batchSize, jobId, valueGenerator);
        this.client = client;
        this.table = table.getBytes(UTF_8);
        this.columnFamily = columnFamily.getBytes(UTF_8);
        this.qualifier = qualifier.getBytes(UTF_8);
    }

    @Override
    protected void onBatchStart() {
        // no op
    }

    @Override
    protected void onBatchCompletion() throws IOException {
        switch (getTaskOperation()) {

        case WRITE:
            // TODO this flushes for all threads...
            client.flush();
            break;

        default:
            break;

        }
    }

    @Override
    protected void onCompletion() throws IOException {
        // no op
    }

    @Override
    protected void onQuanta(byte[] keyBytes, byte[] valueBytes) {
        switch (getTaskOperation()) {

        case WRITE:
            PutRequest request = new PutRequest(table, keyBytes, columnFamily, qualifier,
                valueBytes);
            Deferred<Object> d = client.put(request);
            try {
                logger.info("Put result: " + d.joinUninterruptibly());
            } catch (Exception e) {
                logger.warn("Error while waiting for deferred", e);
            }
            break;

        case READ:
            // TODO
            break;

        }
    }

}
