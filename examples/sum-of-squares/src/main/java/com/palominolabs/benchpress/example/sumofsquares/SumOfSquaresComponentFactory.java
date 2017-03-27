package com.palominolabs.benchpress.example.sumofsquares;

import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.task.reporting.ScopedProgressClient;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

class SumOfSquaresComponentFactory implements ComponentFactory {
    private final SumOfSquaresWorkerConfig config;

    SumOfSquaresComponentFactory(SumOfSquaresWorkerConfig config) {
        this.config = config;
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory() {
        return new TaskFactory() {
            @Nonnull
            @Override
            public Collection<Runnable> getRunnables(@Nonnull UUID jobId, int sliceId, @Nonnull UUID workerId, @Nonnull ScopedProgressClient progressClient) throws IOException {
                return Collections.singleton(() -> {
                    long sum = 0;
                    for (int i = config.first; i <= config.last; i++) {
                        sum += i * i;
                    }

                    // TODO expose sum
                });
            }

            @Override
            public void shutdown() {
                // no op
            }
        };
    }

}
