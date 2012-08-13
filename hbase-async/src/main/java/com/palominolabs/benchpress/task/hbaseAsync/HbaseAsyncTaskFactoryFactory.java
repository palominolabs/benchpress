package com.palominolabs.benchpress.task.hbaseAsync;

import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;
import org.apache.commons.configuration.Configuration;

@Id("HBASE_ASYNC")
final class HbaseAsyncTaskFactoryFactory implements TaskFactoryFactory{
    @Override
    public TaskFactory getTaskFactory(Configuration c) {
        return new HbaseAsyncTaskFactory(c.getString("zkQuorum"), c.getString("table"),
            c.getString("columnFamily"), c.getString("qualifier"));
    }
}
