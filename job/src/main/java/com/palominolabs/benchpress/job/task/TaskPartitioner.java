package com.palominolabs.benchpress.job.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.palominolabs.benchpress.job.json.Partition;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Responsible for splitting a task-type-specific configuration into partitions (one for each worker).
 *
 * Implementations MUST be annotated with {@link com.palominolabs.benchpress.job.id.Id} to be usable from a json job
 * spec.
 */
public interface TaskPartitioner {

    // TODO make this more elegant -- sucks that each impl has to re-serialize by itself

    /**
     * Split this job into partitions.
     *
     * @param jobId        the job id being partitioned
     * @param workers      The number of partitions to create
     * @param progressUrl  The URL to send progress reports to
     * @param finishedUrl  The URL to send finished report to
     * @param objectReader objectReader to use to deserialize config
     * @param configNode   initial task config
     * @param objectWriter objectWriter to use to serialize partition task config
     * @return List of the partitions
     */
    List<Partition> partition(UUID jobId, int workers, String progressUrl, String finishedUrl,
        ObjectReader objectReader, JsonNode configNode, ObjectWriter objectWriter) throws IOException;
}
