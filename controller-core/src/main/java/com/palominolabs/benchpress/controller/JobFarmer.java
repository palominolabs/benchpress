package com.palominolabs.benchpress.controller;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.ipc.Ipc;
import com.palominolabs.benchpress.job.json.Job;
import com.palominolabs.benchpress.job.json.JobSlice;
import com.palominolabs.benchpress.job.json.Task;
import com.palominolabs.benchpress.job.task.JobSlicer;
import com.palominolabs.benchpress.job.task.JobTypePluginRegistry;
import com.palominolabs.benchpress.task.reporting.SliceFinishedReport;
import com.palominolabs.benchpress.task.reporting.SliceProgressReport;
import com.palominolabs.benchpress.worker.WorkerControl;
import com.palominolabs.benchpress.worker.WorkerControlFactory;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.core.Response;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tends to Jobs
 */
@Singleton
@ThreadSafe
public final class JobFarmer {
    private static final Logger logger = LoggerFactory.getLogger(JobFarmer.class);

    private final WorkerControlFactory workerControlFactory;
    private final ServiceProvider<WorkerMetadata> serviceProvider;

    private final UUID controllerId = UUID.randomUUID();

    @GuardedBy("this")
    private final Map<UUID, JobStatus> jobs = new HashMap<>();

    private final JobTypePluginRegistry jobTypePluginRegistry;
    private final ObjectReader objectReader;

    private final ObjectWriter objectWriter;
    // todo make final
    @GuardedBy("this")
    private String httpListenHost;
    @GuardedBy("this")
    private int httpListenPort;

    private static final String REPORT_PATH = "/report";
    private static final String PROGRESS_PATH = REPORT_PATH + "/progress";
    private static final String FINISHED_PATH = REPORT_PATH + "/finished";

    @Inject
    JobFarmer(WorkerControlFactory workerControlFactory,
            ServiceProvider<WorkerMetadata> serviceProvider,
            JobTypePluginRegistry jobTypePluginRegistry, @Ipc ObjectReader objectReader,
            @Ipc ObjectWriter objectWriter) {
        this.workerControlFactory = workerControlFactory;
        this.serviceProvider = serviceProvider;
        this.jobTypePluginRegistry = jobTypePluginRegistry;
        this.objectReader = objectReader;
        this.objectWriter = objectWriter;
    }

    /**
     * Farm out a job to the available workers.
     *
     * @param job The job to cultivate
     * @return 202 on success with Job in the body, 412 on failure
     */
    public Response submitJob(Job job) {

        // Create a set of workers we can lock
        Set<WorkerMetadata> lockedWorkers = new HashSet<>();
        Collection<ServiceInstance<WorkerMetadata>> registeredWorkers;
        try {
            registeredWorkers = serviceProvider.getAllInstances();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (ServiceInstance<WorkerMetadata> instance : registeredWorkers) {
            WorkerMetadata workerMetadata = instance.getPayload();
            WorkerControl workerControl = workerControlFactory.getWorkerControl(workerMetadata);
            if (!workerControl.acquireLock(controllerId)) {
                logger.warn("Unable to lock worker <" + workerControl.getMetadata().getWorkerId() + ">");
                continue;
            }

            lockedWorkers.add(workerMetadata);
        }

        if (lockedWorkers.isEmpty()) {
            logger.warn("No unlocked workers");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("No unlocked workers found").build();
        }

        // TODO unlock locked workers if job slicing, etc. fails to avoid losing workers permanently

        List<Task> tasks;
        try {
            JobSlicer jobSlicer =
                    jobTypePluginRegistry.get(job.getTask().getTaskType()).getControllerComponentFactory(
                            objectReader, job.getTask().getConfigNode()).getJobSlicer();

            tasks = jobSlicer
                    .slice(job.getJobId(), lockedWorkers.size(), getProgressUrl(job.getJobId()),
                            getFinishedUrl(job.getJobId()), objectReader, objectWriter);
        } catch (IOException e) {
            logger.warn("Failed to slice job", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (tasks.isEmpty()) {
            logger.warn("No slices created");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("No slices created").build();
        }

        TandemIterator<Task, WorkerMetadata> titerator = new TandemIterator<>(tasks.iterator(), lockedWorkers.iterator());

        // Submit the slice to the worker
        List<SliceMetadata> jobSlices = new ArrayList<>();
        int sliceId = 0;
        while (titerator.hasNext()) {
            TandemIterator<Task, WorkerMetadata>.Pair pair = titerator.next();
            JobSlice slice = new JobSlice(job.getJobId(), sliceId, pair.first, getProgressUrl(job.getJobId()),
                    getFinishedUrl(job.getJobId()));

            workerControlFactory.getWorkerControl(pair.second).submitSlice(job.getJobId(), slice);
            jobSlices.add(new SliceMetadata(pair.first, pair.second));
        }

        JobStatus jobStatus = new JobStatus(job, jobSlices);

        // Save the JobStatus for our accounting
        synchronized (this) {
            jobs.put(job.getJobId(), jobStatus);
        }

        logger.info("Cultivating job");
        return Response.status(Response.Status.ACCEPTED).entity(job).build();
    }

    /**
     * Get info about a job.
     *
     * @param jobId The job to retrieve
     * @return A Job object corresponding to the given jobId
     */
    public synchronized JobStatusResponse getJobStatus(UUID jobId) {
        return jobs.get(jobId).buildStatusResponse();
    }

    /**
     * Get the jobs that this farmer is cultivating.
     *
     * @return A Set of job IDs
     */
    public synchronized Set<UUID> getJobIds() {
        return jobs.keySet();
    }

    /**
     * Handle a completed slice
     *
     * @param jobId               The jobId that this taskProgressReport is for
     * @param sliceFinishedReport The results data
     * @return ACCEPTED if we handled the taskProgressReport, NOT_FOUND if this farmer doesn't know the given jobId
     */
    public Response handleSliceFinishedReport(UUID jobId, SliceFinishedReport sliceFinishedReport) {

        WorkerControl workerControl;
        synchronized (this) {
            if (!jobs.containsKey(jobId)) {
                logger.warn("Couldn't find job <" + jobId + ">");
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            JobStatus jobStatus = jobs.get(jobId);
            int sliceId = sliceFinishedReport.getSliceId();
            jobStatus.sliceFinished(sliceId, sliceFinishedReport.getDuration());
            logger.info("Slice <" + sliceId + "> finished");

            workerControl = workerControlFactory.getWorkerControl(jobStatus.getWorkerMetadata(sliceId));
        }

        workerControl.releaseLock(controllerId);

        return Response.status(Response.Status.ACCEPTED).build();
    }

    public Response handleSliceProgressReport(UUID jobId, SliceProgressReport sliceProgressReport) {
        synchronized (this) {
            if (!jobs.containsKey(jobId)) {
                logger.warn("Couldn't find job <" + jobId + ">");
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            JobStatus jobStatus = jobs.get(jobId);
            int sliceId = sliceProgressReport.getSliceId();
            jobStatus.sliceProgress(sliceId, sliceProgressReport.getData());
            logger.info("Slice <" + sliceId + "> reported progress");
        }

        return Response.ok().build();
    }

    public synchronized void setListenAddress(String httpListenHost) {
        this.httpListenHost = httpListenHost;
    }

    public synchronized void setListenPort(int httpListenPort) {
        this.httpListenPort = httpListenPort;
    }

    public UUID getControllerId() {
        return controllerId;
    }

    private String getProgressUrl(UUID jobId) {
        return "http://" + httpListenHost + ":" + httpListenPort + "/controller/job/" + jobId + PROGRESS_PATH;
    }

    private String getFinishedUrl(UUID jobId) {
        return "http://" + httpListenHost + ":" + httpListenPort + "/controller/job/" + jobId + FINISHED_PATH;
    }

    final class TandemIterator<S, T> implements Iterator<TandemIterator<S, T>.Pair> {
        private final Iterator<S> iter1;
        private final Iterator<T> iter2;

        TandemIterator(Iterator<S> iter1, Iterator<T> iter2) {
            this.iter1 = iter1;
            this.iter2 = iter2;
        }

        @Override
        public boolean hasNext() {
            return iter1.hasNext() && iter2.hasNext();
        }

        @Override
        public Pair next() {
            return new Pair(iter1.next(), iter2.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        class Pair {
            final S first;
            final T second;

            Pair(S first, T second) {
                this.first = first;
                this.second = second;
            }
        }
    }
}
