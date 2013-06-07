package com.palominolabs.benchpress.job.base.task;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.palominolabs.benchpress.job.id.Id;
import com.palominolabs.benchpress.job.json.Partition;
import com.palominolabs.benchpress.job.json.Task;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactory;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactory;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Convenience base class for TaskFactoryFactory / TaskPartitioner implementations that use TaskConfigBase subclasses.
 */
public abstract class TaskPartitionerBase implements TaskPartitioner {

    private final KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry;
    private final ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry;

    protected TaskPartitionerBase(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        this.keyGeneratorFactoryFactoryRegistry = keyGeneratorFactoryFactoryRegistry;
        this.valueGeneratorFactoryFactoryRegistry = valueGeneratorFactoryFactoryRegistry;
    }

    @Nonnull
    @Override
    public List<Partition> partition(UUID jobId, int workers, String progressUrl, String finishedUrl,
        ObjectReader objectReader, JsonNode configNode, ObjectWriter objectWriter) throws IOException {

        TaskConfigBase c = getConfig();

        List<Partition> partitions = new ArrayList<>();

        int quantaPerPartition = (int) Math.ceil(c.getNumQuanta() / workers);
        for (int partitionId = 0; partitionId < workers; partitionId++) {
            int newQuanta;
            if (partitionId == workers) {
                newQuanta = quantaPerPartition;
            } else {
                newQuanta = c.getNumQuanta() - quantaPerPartition * (workers - 1);
            }

            TaskConfigBase newConfig = c.withNewQuanta(newQuanta);

            TokenBuffer tokBuf = new TokenBuffer(objectReader);
            objectWriter.writeValue(tokBuf, newConfig);
            JsonParser jp = tokBuf.asParser();
            JsonNode newJsonNode =
                objectReader.readValue(jp, JsonNode.class);
            jp.close();

            Partition partition =
                new Partition(jobId, partitionId, new Task(getTaskType(), newJsonNode), progressUrl, finishedUrl);
            partitions.add(partition);
        }

        return partitions;
    }

    /**
     * @return task-impl specific config
     */
    @Nonnull
    protected abstract TaskConfigBase getConfig();

    protected KeyGeneratorFactory getKeyGeneratorFactory(TaskConfigBase config) {
        return keyGeneratorFactoryFactoryRegistry.get(config.getKeyGen().keyGenType).getKeyGeneratorFactory();
    }

    protected ValueGeneratorFactory getValueGeneratorFactory(TaskConfigBase config) {
        return valueGeneratorFactoryFactoryRegistry.get(config.getValueGen().getValueGenType())
            .getFactory(config.getValueGen().getConfig());
    }

    /**
     * @return the task type string that would be included in {@link Id} annotations and the "type" field of task json.
     */
    @Nonnull
    protected abstract String getTaskType();
}
