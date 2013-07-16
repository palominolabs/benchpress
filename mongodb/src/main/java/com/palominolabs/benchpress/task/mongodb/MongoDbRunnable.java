package com.palominolabs.benchpress.task.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.palominolabs.benchpress.job.base.task.AbstractTaskRunnable;
import com.palominolabs.benchpress.job.key.KeyGenerator;
import com.palominolabs.benchpress.job.value.ValueGenerator;

import java.io.IOException;
import java.util.UUID;

final class MongoDbRunnable extends AbstractTaskRunnable implements Runnable {
    private final DB db;
    private final DBCollection collection;

    protected MongoDbRunnable(DB db, String collectionName, KeyGenerator keyGenerator, ValueGenerator valueGenerator,
            UUID jobId, UUID workerId, int partitionId, int numQuanta, int batchSize) {
        super(keyGenerator, workerId, partitionId, numQuanta, batchSize, jobId, valueGenerator);
        this.db = db;
        collection = db.getCollection(collectionName);
    }

    @Override
    protected void onBatchCompletion() throws IOException {
        db.command("{fsync:1}");
    }

    @Override
    protected void onQuanta(byte[] keyBytes, byte[] valueBytes) {
        BasicDBObject dbObject = new BasicDBObject("key", keyBytes);
        dbObject.put("value", valueBytes);
        collection.save(dbObject);
    }

    @Override
    protected void onBatchStart() {
        // no op
    }

    @Override
    protected void onCompletion() throws IOException {
        // no op
    }
}
