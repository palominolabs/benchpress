package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.palominolabs.benchpress.ipc.Ipc;
import com.palominolabs.benchpress.job.registry.JobRegistry;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.UUID;

@ThreadSafe
@Singleton
final class AsyncHttpTaskProgressClient implements TaskProgressClient {
    private static final Logger logger = LoggerFactory.getLogger(AsyncHttpTaskProgressClient.class);
    private final AsyncHttpClient httpClient;
    private final ObjectWriter objectWriter;
    private final JobRegistry jobRegistry;

    @Inject
    AsyncHttpTaskProgressClient(@Ipc AsyncHttpClient httpClient, @Ipc ObjectWriter objectWriter,
        JobRegistry jobRegistry) {
        this.httpClient = httpClient;
        this.objectWriter = objectWriter;
        this.jobRegistry = jobRegistry;
    }

    @Override
    public void reportProgress(final UUID jobId, final int partitionId, final int reportSequenceNum,
        final Duration duration, final int numQuanta) {

        String progressReportUrl = jobRegistry.getProgressUrl(jobId);
        if (progressReportUrl == null) {
            logger.warn("Couldn't get progress url for job id " + jobId);
            return;
        }

        TaskProgressReport report = new TaskProgressReport(partitionId, reportSequenceNum, duration, numQuanta);
        logger.info(
            "Posting progress report for job <" + jobId + "> partition <" + partitionId + "> to <" + progressReportUrl +
                ">");

        try {
            sendPost(progressReportUrl, report, jobId, partitionId, reportSequenceNum);
        } catch (IOException e) {
            logger.warn("Couldn't report progress for jobId " + jobId + ", partition " + partitionId + ", sequence " +
                reportSequenceNum + ", duration " + duration + ", numQuanta " + numQuanta, e);
        }
    }

    @Override
    public void reportFinished(UUID jobId, int partitionId, int reportSequenceNum) {
        String finishedUrl = jobRegistry.getFinishedUrl(jobId);
        if (finishedUrl == null) {
            logger.warn("Couldn't get finished url for job id " + jobId);
            return;
        }

        TaskPartitionFinishedReport report = new TaskPartitionFinishedReport(partitionId, reportSequenceNum);
        logger.info(
            "Posting finished report for job <" + jobId + "> partition <" + partitionId + "> to <" + finishedUrl + ">");

        try {
            sendPost(finishedUrl, report, jobId, partitionId, reportSequenceNum);
        } catch (IOException e) {
            logger.warn("Couldn't report finished for jobId " + jobId + ", partition " + partitionId + ", sequence " +
                reportSequenceNum, e);
        }
    }

    private void sendPost(String url, AbstractTaskReport report, UUID jobId, int partitionId,
        int reportSequenceNum) throws IOException {
        httpClient.preparePost(url).setHeader("Content-Type", "application/json")
            .setBody(objectWriter.writeValueAsBytes(report))
            .execute(new LoggingHandler(jobId, partitionId, reportSequenceNum));
    }

    private static class LoggingHandler extends AsyncCompletionHandler<Response> {

        private final UUID jobId;
        private final int partitionId;
        private final int reportSequenceNum;

        LoggingHandler(UUID jobId, int partitionId, int reportSequenceNum) {
            this.jobId = jobId;
            this.partitionId = partitionId;
            this.reportSequenceNum = reportSequenceNum;
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
            logger.warn(
                "Couldn't report for jobId " + jobId + ", partition " + partitionId + ", sequence " + reportSequenceNum,
                t);
        }
    }
}
