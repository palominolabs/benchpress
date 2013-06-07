package com.palominolabs.benchpress.task.hbase;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.palominolabs.benchpress.job.base.task.TaskFactoryBase;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOperation;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.job.task.TaskOutputQueueProvider;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

final class HbaseTaskFactory extends TaskFactoryBase implements TaskFactory {

    private final String zkQuorum;
    private final String columnFamily;
    private final int port;
    private final String table;
    private final String qualifier;
    /**
     * if true, puts are not buffered by the hbase client
     */
    private final boolean autoFlush;
    /**
     * Only applicable if autoFlush is off.
     */
    private final Long writeBufferSize;

    HbaseTaskFactory(String table, int zkPort, String zkQuorum, String columnFamily, String qualifier,
        boolean autoFlush, Long writeBufferSize, ValueGeneratorFactory valueGeneratorFactory,
        KeyGeneratorFactory keyGeneratorFactory, TaskOperation taskOperation, int numThreads, int numQuanta,
        int batchSize) {
        super(taskOperation, valueGeneratorFactory, batchSize, keyGeneratorFactory, numQuanta, numThreads);
        this.table = table;
        this.port = zkPort;
        this.zkQuorum = zkQuorum;
        this.columnFamily = columnFamily;
        this.qualifier = qualifier;
        this.autoFlush = autoFlush;
        this.writeBufferSize = writeBufferSize;
    }

    @Nonnull
    @Override
    public Collection<Runnable> getRunnables(@Nonnull UUID jobId, int partitionId, @Nonnull UUID workerId,
        @Nonnull TaskOutputQueueProvider taskOutputQueueProvider, @Nullable TaskOutputProcessorFactory taskOutputProcessorFactory) throws IOException {
        List<Runnable> runnables = Lists.newArrayList();

        Configuration hBaseConfiguration = HBaseConfiguration.create();
        hBaseConfiguration.set("hbase.zookeeper.quorum", zkQuorum);
        hBaseConfiguration.set("hbase.zookeeper.property.clientPort", Integer.toString(port));

        int quantaPerThread = numQuanta / numThreads;

        for (int i = 0; i < numThreads; i++) {
            HTable hTable = new HTable(hBaseConfiguration, this.table);
            hTable.setAutoFlush(autoFlush);

            if (writeBufferSize != null) {
                hTable.setWriteBufferSize(writeBufferSize);
            }

            runnables
                .add(
                    new HbaseRunnable(hTable, columnFamily.getBytes(Charsets.UTF_8), qualifier.getBytes(Charsets.UTF_8),
                        keyGeneratorFactory.getKeyGenerator(), valueGeneratorFactory.getValueGenerator(),
                        jobId, workerId, partitionId, quantaPerThread, batchSize
                    ));
        }

        return runnables;
    }

    @Override
    public void shutdown() {
        // no op
    }
}
