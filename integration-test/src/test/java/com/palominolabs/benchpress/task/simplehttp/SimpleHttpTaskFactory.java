package com.palominolabs.benchpress.task.simplehttp;

import com.fasterxml.jackson.databind.node.TextNode;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.task.reporting.TaskProgressClient;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;

import static com.google.common.collect.Lists.newArrayList;

final class SimpleHttpTaskFactory implements TaskFactory {
    private final String url;

    SimpleHttpTaskFactory(String url) {
        this.url = url;
    }

    @Nonnull
    @Override
    public Collection<Runnable> getRunnables(@Nonnull UUID jobId, int sliceId, @Nonnull UUID workerId,
            @Nonnull TaskProgressClient taskProgressClient) throws IOException {
        List<Runnable> runnables = newArrayList();

        runnables.add(() -> {
            AsyncHttpClient client = new AsyncHttpClient();
            try {
                client.prepareGet(url).execute(new AsyncCompletionHandler<Object>() {
                    @Override
                    public Object onCompleted(Response response) throws Exception {
                        taskProgressClient.reportProgress(jobId, sliceId,
                                new TextNode(response.getResponseBody()));
                        return null;
                    }
                }).get();
            } catch (InterruptedException | ExecutionException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        return runnables;
    }

    @Override
    public void shutdown() {
        // no op
    }
}
