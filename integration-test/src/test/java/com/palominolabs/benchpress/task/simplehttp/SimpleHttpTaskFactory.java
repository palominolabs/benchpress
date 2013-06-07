package com.palominolabs.benchpress.task.simplehttp;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.job.task.TaskOutputQueueProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;

final class SimpleHttpTaskFactory implements TaskFactory {
    private final String url;

    public SimpleHttpTaskFactory(String url) {
        this.url = url;
    }

    @Nonnull
    @Override
    public Collection<Runnable> getRunnables(@Nonnull final UUID jobId, int partitionId, @Nonnull UUID workerId,
        @Nonnull final TaskOutputQueueProvider taskOutputQueueProvider,
        @Nullable final TaskOutputProcessorFactory taskOutputProcessorFactory) throws IOException {
        checkNotNull(taskOutputProcessorFactory);

        List<Runnable> runnables = newArrayList();

        runnables.add(new Runnable() {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            @Override
            public void run() {
                AsyncHttpClient client = new AsyncHttpClient();
                try {
                    client.prepareGet(url).execute(new AsyncCompletionHandler<Object>() {
                        @Override
                        public Object onCompleted(Response response) throws Exception {
                            taskOutputQueueProvider.getQueue(jobId, taskOutputProcessorFactory).add("foo");
                            return null;
                        }
                    }).get();
                } catch (InterruptedException | ExecutionException | IOException e) {
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
