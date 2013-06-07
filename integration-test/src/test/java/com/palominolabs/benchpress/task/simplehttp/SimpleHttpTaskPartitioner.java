package com.palominolabs.benchpress.task.simplehttp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.palominolabs.benchpress.job.json.Partition;
import com.palominolabs.benchpress.job.json.Task;
import com.palominolabs.benchpress.job.task.TaskPartitioner;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;

final class SimpleHttpTaskPartitioner implements TaskPartitioner {
    @Nonnull
    @Override
    public List<Partition> partition(UUID jobId, int workers, String progressUrl, String finishedUrl,
        ObjectReader objectReader, JsonNode configNode, ObjectWriter objectWriter) throws IOException {
        return newArrayList(
            new Partition(jobId, 1, new Task(SimpleHttpTaskPlugin.TASK_TYPE, configNode), progressUrl, finishedUrl));
    }
}
