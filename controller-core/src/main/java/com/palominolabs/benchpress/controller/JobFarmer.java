package com.palominolabs.benchpress.controller;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.ipc.Ipc;
import com.palominolabs.benchpress.job.JobStatus;
import com.palominolabs.benchpress.job.SliceStatus;
import com.palominolabs.benchpress.job.json.Job;
import com.palominolabs.benchpress.job.json.JobSlice;
import com.palominolabs.benchpress.job.task.JobSlicer;
import com.palominolabs.benchpress.job.task.JobTypePluginRegistry;
import com.palominolabs.benchpress.task.reporting.SliceFinishedReport;
import com.palominolabs.benchpress.worker.WorkerControl;
import com.palominolabs.benchpress.worker.WorkerControlFactory;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import java.io.IOException;
import java.time.Duration;
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

        List<JobSlice> jobSlices;
        try {
            JobSlicer jobSlicer =
                jobTypePluginRegistry.get(job.getTask().getTaskType()).getControllerComponentFactory(
                    objectReader, job.getTask().getConfigNode()).getJobSlicer();

            jobSlices = jobSlicer
                .slice(job.getJobId(), lockedWorkers.size(), getProgressUrl(job.getJobId()),
                    getFinishedUrl(job.getJobId()), objectReader, job.getTask().getConfigNode(), objectWriter);
        } catch (IOException e) {
            logger.warn("Failed to slice job", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (jobSlices.isEmpty()) {
            logger.warn("No slices created");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("No slices created").build();
        }

        TandemIterator titerator = new TandemIterator(jobSlices.iterator(), lockedWorkers.iterator());
        JobStatus jobStatus = new JobStatus(job);

        // Submit the slice to the worker
        while (titerator.hasNext()) {
            TandemIterator.Pair pair = titerator.next();
            workerControlFactory.getWorkerControl(pair.workerMetadata).submitSlice(job.getJobId(), pair.jobSlice);
            jobStatus.addSliceStatus(new SliceStatus(pair.jobSlice, pair.workerMetadata));
        }

        // Save the JobStatus for our accounting
        jobStatus.setFullySliced();
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
    public synchronized JobStatus getJob(UUID jobId) {
        return jobs.get(jobId);
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
     * @param jobId                       The jobId that this taskProgressReport is for
     * @param sliceFinishedReport The results data
     * @return ACCEPTED if we handled the taskProgressReport, NOT_FOUND if this farmer doesn't know the given jobId
     */
    public Response handleSliceFinishedReport(UUID jobId, SliceFinishedReport sliceFinishedReport) {

        WorkerControl workerControl;
        JobStatus jobStatus;
        synchronized (this) {
            if (!jobs.containsKey(jobId)) {
                logger.warn("Couldn't find job <" + jobId + ">");
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            jobStatus = jobs.get(jobId);
            SliceStatus sliceStatus =
                jobStatus.getSliceStatus(sliceFinishedReport.getSliceId());
            logger.info("Slice <" + sliceStatus.getJobSlice().getSliceId() + "> finished");
            sliceStatus.setFinished(sliceFinishedReport.getDuration());

            workerControl = workerControlFactory.getWorkerControl(sliceStatus.getWorkerMetadata());
        }

        workerControl.releaseLock(controllerId);

        synchronized (this) {
            // Only set the totalDuration of the job when all workers have been started and have finished
            if (jobStatus.isFinished()) {
                Duration totalDuration = Duration.ZERO;
                for (SliceStatus ps : jobStatus.getSliceStatuses().values()) {
                    totalDuration = totalDuration.plus(ps.getDuration());
                }
                jobStatus.setFinalDuration(totalDuration);
            }
        }

        return Response.status(Response.Status.ACCEPTED).build();
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

    final class TandemIterator implements Iterator<TandemIterator.Pair> {
        private final Iterator<JobSlice> piterator;
        private final Iterator<WorkerMetadata> witerator;

        TandemIterator(Iterator<JobSlice> piterator, Iterator<WorkerMetadata> witerator) {
            this.piterator = piterator;
            this.witerator = witerator;
        }

        @Override
        public boolean hasNext() {
            return piterator.hasNext() && witerator.hasNext();
        }

        @Override
        public Pair next() {
            return new Pair(piterator.next(), witerator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        class Pair {
            final JobSlice jobSlice;
            final WorkerMetadata workerMetadata;

            Pair(JobSlice jobSlice, WorkerMetadata workerMetadata) {
                this.jobSlice = jobSlice;
                this.workerMetadata = workerMetadata;
            }
        }
    }
}
