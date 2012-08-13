package com.palominolabs.benchpress.task.mongodb;

import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;
import org.apache.commons.configuration.Configuration;

@Id("MONGODB")
final class MongoDbTaskFactoryFactory implements TaskFactoryFactory {
    @Override
    public TaskFactory getTaskFactory(Configuration c) {
        return new MongoDbTaskFactory(c.getString("hostname"), c.getInt("port"),
            c.getString("dbName"), c.getString("collectionName"));
    }
}
