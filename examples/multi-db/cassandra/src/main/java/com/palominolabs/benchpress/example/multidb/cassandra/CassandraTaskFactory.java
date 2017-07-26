package com.palominolabs.benchpress.example.multidb.cassandra;

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
import com.palominolabs.benchpress.example.multidb.task.TaskFactoryBase;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOperation;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactory;

import com.palominolabs.benchpress.task.reporting.ScopedProgressClient;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

final class CassandraTaskFactory extends TaskFactoryBase implements TaskFactory {

    private final String clusterName;
    private final String keyspaceName;
    private final int port;
    private final String seeds;
    private final String columnFamilyName;
    private final String colName;
    private AstyanaxContext<Keyspace> context;

    CassandraTaskFactory(TaskOperation taskOperation, ValueGeneratorFactory valueGeneratorFactory, int batchSize,
                         KeyGeneratorFactory keyGeneratorFactory, int numQuanta, int numThreads,
        String cluster, String keyspace, int port, String seeds, String columnFamily, String column) {
        super(taskOperation, valueGeneratorFactory, batchSize, keyGeneratorFactory, numQuanta, numThreads);
        this.clusterName = cluster;
        this.keyspaceName = keyspace;
        this.port = port;
        this.seeds = seeds;
        this.columnFamilyName = columnFamily;
        this.colName = column;
    }

    @Nonnull
    @Override
    public Collection<Runnable> getRunnables(@Nonnull UUID jobId, int sliceId, @Nonnull UUID workerId,
            @Nonnull ScopedProgressClient progressClient) throws IOException {

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
                new ColumnFamily<>(columnFamilyName, BytesArraySerializer.get(), BytesArraySerializer.get());

        ArrayList<Runnable> runnables = Lists.newArrayList();

        byte[] colNameBytes = colName.getBytes(Charsets.UTF_8);

        for (int i = 0; i < numThreads; i++) {
            runnables.add(new CassandraRunnable(taskOperation, keyGeneratorFactory.getKeyGenerator(), workerId, sliceId, numQuanta,
                    batchSize, jobId, valueGeneratorFactory.getValueGenerator(), keyspace, cfDef, colNameBytes));
        }

        return runnables;
    }

    @Override
    public void shutdown() {
        context.shutdown();
    }
}
