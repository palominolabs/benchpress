package com.palominolabs.benchpress.task.mongodb;

import com.google.common.collect.Lists;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.palominolabs.benchpress.job.base.task.TaskFactoryBase;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOperation;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactory;
import com.palominolabs.benchpress.task.reporting.TaskProgressClient;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

final class MongoDbTaskFactory extends TaskFactoryBase implements TaskFactory {
    private final String hostname;
    private final int port;
    private final String dbName;
    private String collectionName;
    private Mongo mongo;

    MongoDbTaskFactory(TaskOperation taskOperation, ValueGeneratorFactory valueGeneratorFactory, int batchSize,
        KeyGeneratorFactory keyGeneratorFactory, int numQuanta, int numThreads, int progressReportInterval,
        String hostname, int port, String dbName, String collectionName) {
        super(taskOperation, valueGeneratorFactory, batchSize, keyGeneratorFactory, numQuanta, numThreads,
            progressReportInterval);
        this.hostname = hostname;
        this.port = port;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    @Override
    public Collection<Runnable> getRunnables(UUID jobId, int partitionId, UUID workerId,
        TaskProgressClient taskProgressClient, AtomicInteger reportSequenceCounter) throws IOException {

        mongo = new Mongo(this.hostname, this.port);
        int quantaPerThread = numQuanta / numThreads;

        List<Runnable> runnables = Lists.newArrayList();
        for (int i = 0; i < numThreads; i++) {
            DB db = mongo.getDB(this.dbName);

            runnables.add(new MongoDbRunnable(db, collectionName,
                keyGeneratorFactory.getKeyGenerator(), valueGeneratorFactory.getValueGenerator(),
                taskProgressClient, reportSequenceCounter,
                jobId, workerId, partitionId, quantaPerThread, batchSize, progressReportInterval
            ));
        }

        return runnables;
    }

    @Override
    public void shutdown() {
        mongo.close();
    }
}
