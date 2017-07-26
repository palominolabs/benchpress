package com.palominolabs.benchpress.example.multidb.task;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.palominolabs.benchpress.job.id.Identifiable;
import com.palominolabs.benchpress.job.json.JobSlice;
import com.palominolabs.benchpress.job.json.Task;
import com.palominolabs.benchpress.job.task.JobSlicer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

/**
 * Convenience base class for TaskFactoryFactory / JobSlicer implementations that use TaskConfigBase subclasses.
 */
public abstract class JobSlicerBase implements JobSlicer {

    @Nonnull
    @Override
    public List<Task> slice(UUID jobId, int workers, String progressUrl, String finishedUrl,
            ObjectReader objectReader, JsonNode configNode, ObjectWriter objectWriter) throws IOException {

        TaskConfigBase c = getConfig();

        List<Task> tasks = new ArrayList<>();

        int quantaPerSlice = (int) Math.ceil(c.getNumQuanta() / workers);
        for (int sliceId = 0; sliceId < workers; sliceId++) {
            int newQuanta;
            // TODO
            if (sliceId == workers) {
                newQuanta = quantaPerSlice;
            } else {
                newQuanta = c.getNumQuanta() - quantaPerSlice * (workers - 1);
            }

            TaskConfigBase newConfig = c.withNewQuanta(newQuanta);

            TokenBuffer tokBuf = new TokenBuffer(objectReader, false);
            objectWriter.writeValue(tokBuf, newConfig);
            JsonParser jp = tokBuf.asParser();
            JsonNode newJsonNode =
                    objectReader.readValue(jp, JsonNode.class);
            jp.close();

            Task task = new Task(getTaskType(), newJsonNode);
            tasks.add(task);
        }

        return tasks;
    }

    /**
     * @return task-impl specific config
     */
    @Nonnull
    protected abstract TaskConfigBase getConfig();

    /**
     * @return the task type string that would be in the plugin's {@link Identifiable} implementation and the "type"
     * field of task json.
     */
    @Nonnull
    protected abstract String getTaskType();
}
