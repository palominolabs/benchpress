package com.palominolabs.benchpress.worker;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Throwables;
import com.palominolabs.benchpress.job.json.Partition;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public final class WorkerControl {
    private static final Logger logger = LoggerFactory.getLogger(WorkerControl.class);

    private WorkerMetadata metadata;
    private HttpClient httpClient;
    private ObjectWriter objectWriter;
    private ObjectReader objectReader;

    WorkerControl(WorkerMetadata workerMetadata, HttpClient httpClient, ObjectWriter objectWriter, ObjectReader objectReader) {
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
        HttpPost httpPost = new HttpPost(lockUri);

        boolean locked = tellWorker(httpPost, Response.Status.NO_CONTENT);
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
        HttpPost httpPost = new HttpPost(unLockUri);

        boolean released = tellWorker(httpPost, Response.Status.NO_CONTENT);
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
    public String locker() {
        return getLockStatus().getControllerId();
    }

    /**
     * Submit a job to this worker.
     *
     * @param jobId The job that this partition is part of
     * @param partition The partition fot the worker to do
     * @return true if the partition was successfully submitted
     */
    public boolean submitPartition(UUID jobId, Partition partition) {
        String submitUri = getUrlPrefix() + "/worker/job/" + jobId + "/partition";
        HttpPut httpPut = new HttpPut(submitUri);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(objectWriter.writeValueAsString(partition));
            stringEntity.setContentType(MediaType.APPLICATION_JSON);
        } catch (IOException e) {
            logger.warn("Unable to JSONify partition <" + partition.getPartitionId() + "> for jobId <" + jobId + ">");
        }
        httpPut.setEntity(stringEntity);

        logger.info("Sending partition <" + partition.getPartitionId() + "> of jobId <" + jobId + "> to <" + metadata
            .getWorkerId() + ">");
        return tellWorker(httpPut, Response.Status.ACCEPTED);
    }

    private String getUrlPrefix() {
        return "http://" + metadata.getListenAddress() + ":" + metadata.getListenPort();
    }

    private LockStatus getLockStatus() {
        String unLockUri = getUrlPrefix() + "/worker/control/lockStatus";
        HttpGet httpGet = new HttpGet(unLockUri);
        try {
            return objectReader.withType(LockStatus.class).readValue(askWorker(httpGet));
        } catch (IOException e) {
            logger.warn("Error reading worker lock status");
            throw Throwables.propagate(e);
        }
    }

    /**
     * Send an HTTP message to a worker, producing helpful logging if there was a problem
     *
     * @param uriRequest The request to make
     * @param expectedStatus The expected return status
     * @return true if the method was successfully delivered & the worker gave the expected response
     */
    private boolean tellWorker(HttpUriRequest uriRequest, Response.Status expectedStatus) {
        try {
            HttpResponse response = httpClient.execute(uriRequest);

            if (response.getStatusLine().getStatusCode() != expectedStatus.getStatusCode()) {
                StatusLine statusLine = response.getStatusLine();
                EntityUtils.consume(response.getEntity());
                logger.warn("Problem telling worker <" + metadata.getWorkerId() + "> " + "(" + uriRequest.getURI() + "), " +
                    "reason [" + statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase() + "]");
                return false;
            }

            return true;
        } catch (IOException e) {
            logger.warn("Unable to communicated with worker " + metadata.toString());
            return false;
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
    private InputStream askWorker(HttpUriRequest uriRequest) {
        try {
            HttpResponse response = httpClient.execute(uriRequest);

            if (response.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode()) {
                StatusLine statusLine = response.getStatusLine();
                EntityUtils.consume(response.getEntity());
                logger.warn("Problem asking worker <" + metadata.getWorkerId() + "> " + "(" + uriRequest.getURI() + "), " +
                    "reason [" + statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase() + "]");
            }

            return response.getEntity().getContent();
        } catch (IOException e) {
            logger.warn("Unable to communicated with worker " + metadata.toString());
            throw Throwables.propagate(e);
        }
    }
}
