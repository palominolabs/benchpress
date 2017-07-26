package com.palominolabs.benchpress.job.registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.UUID;

/**
 * Used by workers to keep track of running jobs
 */
@ThreadSafe
public interface JobRegistry {

    void storeJob(UUID jobId, String progressUrl, String progressReportUrl);

    /**
     * @param jobId job id
     * @return if the job exists in the registry, the url to send progress reports to
     */
    @Nonnull
    String getProgressUrl(UUID jobId);

    /**
     * @param jobId job id
     * @return if the job exists in the registry, the url to send finished reports to
     */
    @Nonnull
    String getFinishedUrl(UUID jobId);

    /**
     * When a worker is done with a job, the job should be removed from the registry.
     *
     * @param jobId job id
     */
    void removeJob(UUID jobId);
}
