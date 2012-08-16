package com.palominolabs.benchpress.task.mongodb;

import com.google.common.collect.Lists;
import com.mongodb.DB;
import com.mongodb.Mongo;
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

final class MongoDbTaskFactory implements TaskFactory {
    private final String hostname;
    private final int port;
    private final String dbName;
    private String collectionName;
    private Mongo mongo;

    public MongoDbTaskFactory(MongoDbTaskFactoryFactory.MongoDbConfig config) {
        this.hostname = config.hostname;
        this.port = config.port;
        this.dbName = config.dbName;
        this.collectionName = config.collectionName;
    }

    @Override
    public Collection<Runnable> getRunnables(KeyGeneratorFactory keyGeneratorFactory,
        ValueGeneratorFactory valueGeneratorFactory, TaskOperation taskOperation, int numThreads, int numQuanta,
        int batchSize, UUID workerId, int partitionId, TaskProgressClient taskProgressClient, UUID jobId,
        int progressReportInterval, AtomicInteger reportSequenceCounter) throws IOException {

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
