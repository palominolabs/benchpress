package com.palominolabs.benchpress.job.task;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import com.palominolabs.benchpress.job.json.Task;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Responsible for splitting a task-type-specific configuration into slices (one for each worker).
 */
public interface JobSlicer {

    /**
     * Split this job into slices, one for each worker.
     *
     * @param jobId        the job id being sliced
     * @param workers      The number of slices to create
     * @param progressUrl  The URL to send progress reports to
     * @param finishedUrl  The URL to send finished report to
     * @param objectReader objectReader configured like objectWriter
     * @param objectWriter objectWriter to use to serialize slice task config
     * @return List of the slices
     * @throws IOException if slicing fails
     */
    @Nonnull
    List<Task> slice(UUID jobId, int workers, String progressUrl, String finishedUrl,
                     ObjectReader objectReader, ObjectWriter objectWriter) throws IOException;
}
