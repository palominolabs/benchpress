package com.palominolabs.benchpress.worker;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.ning.http.client.AsyncHttpClient;
import com.palominolabs.benchpress.job.json.JobSlice;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WorkerControl {
    private static final Logger logger = LoggerFactory.getLogger(WorkerControl.class);

    private WorkerMetadata metadata;
    private AsyncHttpClient httpClient;
    private ObjectWriter objectWriter;
    private ObjectReader objectReader;

    WorkerControl(WorkerMetadata workerMetadata, AsyncHttpClient httpClient, ObjectWriter objectWriter,
        ObjectReader objectReader) {
        this.metadata = workerMetadata;
        this.httpClient = httpClient;
        this.objectWriter = objectWriter;
        this.objectReader = objectReader;
    }

    public WorkerMetadata getMetadata() {
        return metadata;
    }

    /**
     * @param controllerId The controller acquiring the lock
     * @return true if the worker was successfully locked, false otherwise
     */
    public boolean acquireLock(UUID controllerId) {
        logger.info("Acquiring lock of worker <" + metadata.getWorkerId() + ">");
        String lockUri = getUrlPrefix() + "/worker/control/acquireLock/" + controllerId;

        boolean locked = tellWorker(httpClient.preparePost(lockUri), Response.Status.NO_CONTENT);
        if (locked) {
            logger.info("Successfully acquired lock of worker <" + metadata.getWorkerId() + ">");
        } else {
            logger.warn("Acquiring lock of worker <" + metadata.getWorkerId() + "> failed");
        }

        return locked;
    }

    /**
     * @param controllerId The controller acquiring the lock
     * @return true if the lock successfully released, false otherwise
     */
    public boolean releaseLock(UUID controllerId) {
        logger.info("Releasing lock of worker <" + metadata.getWorkerId() + ">");
        String unLockUri = getUrlPrefix() + "/worker/control/releaseLock/" + controllerId;

        boolean released = tellWorker(httpClient.preparePost(unLockUri), Response.Status.NO_CONTENT);
        if (released) {
            logger.info("Successfully released lock of worker <" + metadata.getWorkerId() + ">");
        } else {
            logger.warn("Releasing lock  of worker <" + metadata.getWorkerId() + "> failed");
        }

        return released;
    }

    /**
     * @return true if the worker is locked
     */
    public boolean isLocked() {
        return getLockStatus().isLocked();
    }

    /**
     * @return the controller that has this worker locked, null if the worker is not locked
     */
    public UUID locker() {
        return getLockStatus().getControllerId();
    }

    /**
     * Submit a job to this worker.
     *
     * @param jobId     The job that this slice is part of
     * @param jobSlice The slice fot the worker to do
     * @return true if the slice was successfully submitted
     */
    public boolean submitSlice(UUID jobId, JobSlice jobSlice) {
        String submitUri = getUrlPrefix() + "/worker/job/" + jobId + "/slice";

        AsyncHttpClient.BoundRequestBuilder req;
        try {
            req = httpClient.preparePut(submitUri).setBody(objectWriter.writeValueAsString(jobSlice))
                .addHeader("Content-Type", MediaType.APPLICATION_JSON);
        } catch (IOException e) {
            logger.warn("Unable to JSONify slice <" + jobSlice.getSliceId() + "> for jobId <" + jobId + ">");
            return false;
        }

        logger.info("Sending slice <" + jobSlice.getSliceId() + "> of jobId <" + jobId + "> to <" + metadata
            .getWorkerId() + ">");
        return tellWorker(req, Response.Status.ACCEPTED);
    }

    private String getUrlPrefix() {
        return "http://" + metadata.getListenAddress() + ":" + metadata.getListenPort();
    }

    private LockStatus getLockStatus() {
        String unLockUri = getUrlPrefix() + "/worker/control/lockStatus";
        try {
            return objectReader.forType(LockStatus.class).readValue(askWorker(httpClient.prepareGet(unLockUri)));
        } catch (IOException e) {
            logger.warn("Error reading worker lock status");
            throw new RuntimeException(e);
        }
    }

    /**
     * Send an HTTP message to a worker, producing helpful logging if there was a problem
     *
     * @param uriRequest     The request to make
     * @param expectedStatus The expected return status
     * @return true if the method was successfully delivered & the worker gave the expected response
     */
    private boolean tellWorker(AsyncHttpClient.BoundRequestBuilder uriRequest, Response.Status expectedStatus) {
        try {
            com.ning.http.client.Response response = uriRequest.execute().get();
            if (response.getStatusCode() != expectedStatus.getStatusCode()) {
                logger.warn(
                    "Problem telling worker <" + metadata.getWorkerId() + "> " + "(" + response.getUri() + "), " +
                        "reason [" + response.getStatusCode() + ": " + response.getStatusText() + "]");
                return false;
            }

            return true;
        } catch (IOException | ExecutionException e) {
            logger.warn("Unable to communicated with worker " + metadata.toString());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Interrupted");
            throw new RuntimeException(e);
        }
    }

    /**
     * Send an HTTP message to a worker and get the result
     *
     * Note: expects the worker to respond with OK (200) status code.
     *
     * @param uriRequest The request to make
     * @return An InputStream of the response content
     */
    private InputStream askWorker(AsyncHttpClient.BoundRequestBuilder uriRequest) {
        try {
            com.ning.http.client.Response response = uriRequest.execute().get();

            if (response.getStatusCode() != Response.Status.OK.getStatusCode()) {
                logger.warn(
                    "Problem asking worker <" + metadata.getWorkerId() + "> " + "(" + response.getUri() + "), " +
                        "reason [" + response.getStatusCode() + ": " + response.getStatusText() + "]");
            }

            return response.getResponseBodyAsStream();
        } catch (IOException | ExecutionException e) {
            logger.warn("Unable to communicated with worker " + metadata.toString());
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Interrupted");
            throw new RuntimeException(e);
        }
    }
}
