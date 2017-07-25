package com.palominolabs.benchpress.example.multidb.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.palominolabs.benchpress.example.multidb.task.AbstractTaskRunnable;
import com.palominolabs.benchpress.example.multidb.key.KeyGenerator;
import com.palominolabs.benchpress.job.task.TaskOperation;
import com.palominolabs.benchpress.example.multidb.value.ValueGenerator;

import java.io.IOException;
import java.util.UUID;

final class MongoDbRunnable extends AbstractTaskRunnable implements Runnable {
    private final DB db;
    private final DBCollection collection;

    protected MongoDbRunnable(TaskOperation taskOperation, DB db, String collectionName,
            KeyGenerator keyGenerator, ValueGenerator valueGenerator, UUID jobId, UUID workerId,
            int sliceId, int numQuanta, int batchSize) {
        super(taskOperation, keyGenerator, workerId, sliceId, numQuanta, batchSize, jobId, valueGenerator);
        this.db = db;
        collection = db.getCollection(collectionName);
    }

    @Override
    protected void onBatchStart() {
        // no op
    }

    @Override
    protected void onBatchCompletion() throws IOException {
        switch (getTaskOperation()) {

        case WRITE:
            db.command("{fsync:1}");
            break;

        default:
            break;

        }
    }

    @Override
    protected void onQuanta(byte[] keyBytes, byte[] valueBytes) {
        BasicDBObject dbObject = new BasicDBObject("key", keyBytes);

        switch (getTaskOperation()) {

        case WRITE:
            dbObject.put("value", valueBytes);
            collection.save(dbObject);
            break;

        case READ:
            // TODO
            break;

        }
    }

    @Override
    protected void onCompletion() throws IOException {
        // no op
    }
}
