package com.palominolabs.benchpress.job.base.task;

import com.palominolabs.benchpress.job.key.KeyGeneratorFactory;
import com.palominolabs.benchpress.job.task.TaskOperation;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactory;

/**
 * Convenience base class for the default task factories.
 */
public abstract class TaskFactoryBase {
    protected final ValueGeneratorFactory valueGeneratorFactory;
    protected final KeyGeneratorFactory keyGeneratorFactory;
    protected final TaskOperation taskOperation;
    protected final int numThreads;
    protected final int progressReportInterval;
    protected final int numQuanta;
    protected final int batchSize;

    protected TaskFactoryBase(TaskOperation taskOperation, ValueGeneratorFactory valueGeneratorFactory, int batchSize,
        KeyGeneratorFactory keyGeneratorFactory, int numQuanta, int numThreads, int progressReportInterval) {
        this.taskOperation = taskOperation;
        this.valueGeneratorFactory = valueGeneratorFactory;
        this.batchSize = batchSize;
        this.keyGeneratorFactory = keyGeneratorFactory;
        this.numQuanta = numQuanta;
        this.numThreads = numThreads;
        this.progressReportInterval = progressReportInterval;
    }

}
