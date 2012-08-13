package com.palominolabs.benchpress.task.hbase;

import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;
import org.apache.commons.configuration.Configuration;

@Id("HBASE")
final class HbaseTaskFactoryFactory implements TaskFactoryFactory {
    @Override
    public TaskFactory getTaskFactory(Configuration c) {
        return new HbaseTaskFactory(c.getString("zkQuorum"), c.getInt("zkPort"),
            c.getString("table"), c.getString("columnFamily"), c.getString("qualifier"),
            c.getBoolean("autoFlush"), c.getLong("writeBufferSize", null));
    }
}
