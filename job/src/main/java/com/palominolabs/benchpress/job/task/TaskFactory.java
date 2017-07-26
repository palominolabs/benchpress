package com.palominolabs.benchpress.job.task;

import com.palominolabs.benchpress.task.reporting.TaskProgressClient;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

/**
 * A TaskFactory creates the runnables that actually do the work. Used to create the runnables for one individual
 * slice.
 */
@NotThreadSafe
public interface TaskFactory {

    /**
     * @param jobId                      job id
     * @param sliceId                    the slice of the overall job that these tasks are part of
     * @param workerId                   the worker that these tasks are running in
     * @return runnables
     * @throws IOException if task creation fails
     */
    @Nonnull
    Collection<Runnable> getRunnables(@Nonnull UUID jobId, int sliceId, @Nonnull UUID workerId,
            @Nonnull TaskProgressClient taskProgressClient) throws
            IOException;

    void shutdown();
}
