package com.palominolabs.benchpress.task.cassandra;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.Slf4jConnectionPoolMonitorImpl;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.BytesArraySerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOperation;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactory;
import com.palominolabs.benchpress.task.reporting.TaskProgressClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

final class CassandraTaskFactory implements TaskFactory {

    private final String clusterName;
    private final String keyspaceName;
    private final int port;
    private final String seeds;
    private final String columnFamilyName;
    private final String colName;
    private AstyanaxContext<Keyspace> context;

    public CassandraTaskFactory(String clusterName, String keyspaceName, int port, String seeds,
        String columnFamilyName, String colName) {
        this.clusterName = clusterName;
        this.keyspaceName = keyspaceName;
        this.port = port;
        this.seeds = seeds;
        this.columnFamilyName = columnFamilyName;
        this.colName = colName;
    }

    @Override
    public Collection<Runnable> getRunnables(KeyGeneratorFactory keyGeneratorFactory,
        ValueGeneratorFactory valueGeneratorFactory, TaskOperation taskOperation, int numThreads, int numQuanta,
        int batchSize, UUID workerId, int partitionId, TaskProgressClient taskProgressClient, UUID jobId,
        int progressReportInterval, AtomicInteger reportSequenceCounter) throws IOException {

        context = new AstyanaxContext.Builder().forCluster(clusterName)
            .forKeyspace(keyspaceName)
            .withAstyanaxConfiguration(new AstyanaxConfigurationImpl().setDiscoveryType(NodeDiscoveryType.NONE))
            .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl("defaultConnPool")
                .setPort(port)
                .setMaxConnsPerHost(100)
                .setSeeds(seeds))
            .withConnectionPoolMonitor(new Slf4jConnectionPoolMonitorImpl())
            .buildKeyspace(ThriftFamilyFactory.getInstance());

        context.start();

        Keyspace keyspace = context.getEntity();

        ColumnFamily<byte[], byte[]> cfDef =
            new ColumnFamily<byte[], byte[]>(columnFamilyName, BytesArraySerializer.get(), BytesArraySerializer.get());

        ArrayList<Runnable> runnables = Lists.newArrayList();

        byte[] colNameBytes = colName.getBytes(Charsets.UTF_8);

        for (int i = 0; i < numThreads; i++) {
            runnables.add(new CassandraRunnable(keyGeneratorFactory.getKeyGenerator(), workerId, partitionId, numQuanta,
                batchSize, progressReportInterval, taskProgressClient, jobId, valueGeneratorFactory.getValueGenerator(),
                reportSequenceCounter, keyspace, cfDef, colNameBytes));
        }

        return runnables;
    }

    @Override
    public void shutdown() {
        context.shutdown();
    }
}
