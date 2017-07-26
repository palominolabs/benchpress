package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.palominolabs.benchpress.ipc.Ipc;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThreadSafe
@Singleton
final class AsyncHttpTaskProgressClient implements TaskProgressClient {
    private static final Logger logger = LoggerFactory.getLogger(AsyncHttpTaskProgressClient.class);
    private final AsyncHttpClient httpClient;
    private final ObjectWriter objectWriter;

    @Inject
    AsyncHttpTaskProgressClient(@Ipc AsyncHttpClient httpClient, @Ipc ObjectWriter objectWriter) {
        this.httpClient = httpClient;
        this.objectWriter = objectWriter;
    }

    @Override
    public void reportFinished(UUID jobId, int sliceId, Duration duration, String url) {

        SliceFinishedReport report = new SliceFinishedReport(sliceId, duration);
        logger.info(
            "Posting finished report for job <" + jobId + "> slice <" + sliceId + "> to <" + url + ">");

        try {
            sendPost(url, report, jobId, sliceId);
        } catch (IOException e) {
            logger.warn("Couldn't report finished for jobId " + jobId + ", slice " + sliceId, e);
        }
    }

    @Override
    public void reportProgress(UUID jobId, int sliceId, JsonNode data, String url) {
        SliceProgressReport report = new SliceProgressReport(sliceId, data);
        logger.info(
                "Posting progress report for job <" + jobId + "> slice <" + sliceId + "> to <" + url + ">");

        try {
            sendPost(url, report, jobId, sliceId);
        } catch (IOException e) {
            logger.warn("Couldn't report progress for jobId " + jobId + ", slice " + sliceId, e);
        }
    }

    private void sendPost(String url, AbstractTaskReport report, UUID jobId, int sliceId) throws IOException {
        httpClient.preparePost(url).setHeader("Content-Type", "application/json")
            .setBody(objectWriter.writeValueAsBytes(report))
            .execute(new LoggingHandler(jobId, sliceId));
    }

    private static class LoggingHandler extends AsyncCompletionHandler<Response> {

        private final UUID jobId;
        private final int sliceId;

        LoggingHandler(UUID jobId, int sliceId) {
            this.jobId = jobId;
            this.sliceId = sliceId;
        }

        @Override
        public Response onCompleted(Response response) throws Exception {
            if (response.getStatusCode() != 202) {
                logger.warn("Got status code " + response.getStatusCode() + ": <" + response.getStatusText() + ">");
            }
            return null;
        }

        @Override
        public void onThrowable(Throwable t) {
            logger.warn("Couldn't report for jobId " + jobId + ", slice " + sliceId, t);
        }
    }
}
