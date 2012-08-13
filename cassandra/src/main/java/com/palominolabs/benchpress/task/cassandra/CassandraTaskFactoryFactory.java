package com.palominolabs.benchpress.task.cassandra;

import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;
import org.apache.commons.configuration.Configuration;

@Id("CASSANDRA")
final class CassandraTaskFactoryFactory implements TaskFactoryFactory {
    @Override
    public TaskFactory getTaskFactory(Configuration c) {
        return new CassandraTaskFactory(c.getString("cluster"), c.getString("keyspace"), c.getInt("port"),
            c.getString("seeds"), c.getString("columnFamily"), c.getString("column"));
    }
}
