package com.palominolabs.benchpress.task.hbase;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOperation;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactory;
import com.palominolabs.benchpress.task.reporting.TaskProgressClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

final class HbaseTaskFactory implements TaskFactory {

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
    private Long writeBufferSize;

    public HbaseTaskFactory(HbaseTaskFactoryFactory.HBaseConfig config) {
        this.table = config.table;
        this.port = config.zkPort;
        this.zkQuorum = config.zkQuorum;
        this.columnFamily = config.columnFamily;
        this.qualifier = config.qualifier;
        this.autoFlush = config.autoFlush;
        this.writeBufferSize = config.writeBufferSize;
    }

    @Override
    public Collection<Runnable> getRunnables(KeyGeneratorFactory keyGeneratorFactory,
        ValueGeneratorFactory valueGeneratorFactory, TaskOperation taskOperation, int numThreads, int numQuanta,
        int batchSize, UUID workerId, int partitionId, TaskProgressClient taskProgressClient, UUID jobId,
        int progressReportInterval, AtomicInteger reportSequenceCounter) throws IOException {

        List<Runnable> runnables = Lists.newArrayList();

        Configuration hBaseConfiguration = HBaseConfiguration.create();
        hBaseConfiguration.set("hbase.zookeeper.quorum", zkQuorum);
        hBaseConfiguration.set("hbase.zookeeper.property.clientPort", Integer.toString(port));

        int quantaPerThread = numQuanta / numThreads;

        for (int i = 0; i < numThreads; i++) {
            HTable table = new HTable(hBaseConfiguration, this.table);
            table.setAutoFlush(autoFlush);

            if (writeBufferSize != null) {
                table.setWriteBufferSize(writeBufferSize);
            }

            runnables
                .add(new HbaseRunnable(table, columnFamily.getBytes(Charsets.UTF_8), qualifier.getBytes(Charsets.UTF_8),
                    keyGeneratorFactory.getKeyGenerator(), valueGeneratorFactory.getValueGenerator(),
                    taskProgressClient, reportSequenceCounter,
                    jobId, workerId, partitionId, quantaPerThread, batchSize, progressReportInterval
                ));
        }

        return runnables;
    }

    @Override
    public void shutdown() {
        // no op
    }
}
