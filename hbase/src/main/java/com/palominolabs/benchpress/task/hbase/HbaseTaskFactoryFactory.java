package com.palominolabs.benchpress.task.hbase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.inject.Inject;
import com.palominolabs.benchpress.job.base.task.TaskFactoryFactoryPartitionerBase;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import java.io.IOException;

@Id(HbaseTaskFactoryFactory.TASK_TYPE)
final class HbaseTaskFactoryFactory extends TaskFactoryFactoryPartitionerBase implements TaskFactoryFactory, TaskPartitioner {

    static final String TASK_TYPE = "HBASE";

    @Inject
    HbaseTaskFactoryFactory(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        super(keyGeneratorFactoryFactoryRegistry, valueGeneratorFactoryFactoryRegistry);
    }

    @Override
    public TaskFactory getTaskFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        HBaseConfig c = getConfig(objectReader, configNode);

        return new HbaseTaskFactory(c.getTable(), c.getZkPort(), c.getZkQuorum(), c.getColumnFamily(), c.getQualifier(),
            c.isAutoFlush(), c.getWriteBufferSize(), getValueGeneratorFactory(c), getKeyGeneratorFactory(c),
            c.getTaskOperation(), c.getNumThreads(),
            c.getNumQuanta(), c.getBatchSize(), c.getProgressReportInterval());
    }

    @Nonnull
    @Override
    protected HBaseConfig getConfig(ObjectReader objectReader, JsonNode configNode) throws IOException {
        return objectReader.withType(HBaseConfig.class).readValue(configNode);
    }

    @Nonnull
    @Override
    protected String getTaskType() {
        return TASK_TYPE;
    }
}
