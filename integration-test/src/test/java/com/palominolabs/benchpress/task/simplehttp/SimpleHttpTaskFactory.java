package com.palominolabs.benchpress.task.simplehttp;

import com.ning.http.client.AsyncHttpClient;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.task.reporting.TaskProgressClient;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;

final class SimpleHttpTaskFactory implements TaskFactory {
    private final String url;

    public SimpleHttpTaskFactory(String url) {
        this.url = url;
    }

    @Nonnull
    @Override
    public Collection<Runnable> getRunnables(UUID jobId, int partitionId, UUID workerId,
        TaskProgressClient taskProgressClient, AtomicInteger reportSequenceCounter) throws IOException {
        List<Runnable> runnables = newArrayList();
        runnables.add(new Runnable() {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            @Override
            public void run() {
                AsyncHttpClient client = new AsyncHttpClient();
                try {
                    client.prepareGet(url).execute().get();
                } catch (InterruptedException e) {
                    propagate(e);
                } catch (ExecutionException e) {
                    propagate(e);
                } catch (IOException e) {
                    propagate(e);
                }
            }
        });
        return runnables;
    }

    @Override
    public void shutdown() {
        // no op
    }
}
