package com.palominolabs.benchpress.example.multidb.task;

import com.palominolabs.benchpress.job.json.KeyGen;
import com.palominolabs.benchpress.job.json.ValueGen;
import com.palominolabs.benchpress.job.task.TaskOperation;

import javax.annotation.concurrent.Immutable;

/**
 * Default base class for configuration to avoid code duplication across several of the default task types. Custom job
 * types may make use of this if desired, but are free to define their configuration howerever they please.
 *
 * Subclasses should be immutable, and should annotate their constructors with @JsonCreator and @JsonProperty as
 * applicable.
 */
@Immutable
public abstract class TaskConfigBase {

    private final TaskOperation taskOperation;

    private final int numThreads;

    private final int numQuanta;

    private final int batchSize;

    private final KeyGen keyGen;

    private final ValueGen valueGen;

    protected TaskConfigBase(TaskOperation taskOperation, int numThreads, int numQuanta, int batchSize, KeyGen keyGen,
        ValueGen valueGen) {
        this.taskOperation = taskOperation;
        this.numThreads = numThreads;
        this.numQuanta = numQuanta;
        this.batchSize = batchSize;
        this.keyGen = keyGen;
        this.valueGen = valueGen;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public KeyGen getKeyGen() {
        return keyGen;
    }

    public int getNumQuanta() {
        return numQuanta;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public TaskOperation getTaskOperation() {
        return taskOperation;
    }

    public ValueGen getValueGen() {
        return valueGen;
    }

    /**
     * @param newQuanta the new quanta to use
     * @return a copy of this with a different quanta
     */
    public abstract TaskConfigBase withNewQuanta(int newQuanta);
}
