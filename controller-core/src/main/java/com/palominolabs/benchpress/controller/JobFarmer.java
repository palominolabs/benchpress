package com.palominolabs.benchpress.controller;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.ipc.Ipc;
import com.palominolabs.benchpress.job.JobStatus;
import com.palominolabs.benchpress.job.PartitionStatus;
import com.palominolabs.benchpress.job.json.Job;
import com.palominolabs.benchpress.job.json.Partition;
import com.palominolabs.benchpress.job.task.TaskPartitioner;
import com.palominolabs.benchpress.job.task.TaskPluginRegistry;
import com.palominolabs.benchpress.task.reporting.TaskPartitionFinishedReport;
import com.palominolabs.benchpress.worker.WorkerControl;
import com.palominolabs.benchpress.worker.WorkerControlFactory;
import com.palominolabs.benchpress.worker.WorkerFinder;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import org.apache.curator.x.discovery.ServiceInstance;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tends to Jobs
 */
@Singleton
@ThreadSafe
public final class JobFarmer {
    private static final Logger logger = LoggerFactory.getLogger(JobFarmer.class);

    private final WorkerFinder workerFinder;
    private final WorkerControlFactory workerControlFactory;

    private final UUID controllerId = UUID.randomUUID();

    private final Map<UUID, JobStatus> jobs = new HashMap<>();

    private final TaskPluginRegistry taskPluginRegistry;
    private final ObjectReader objectReader;

    private final ObjectWriter objectWriter;
    // todo make final
    private String httpListenHost;
    private int httpListenPort;

    private static final String REPORT_PATH = "/report";
    private static final String PROGRESS_PATH = REPORT_PATH + "/progress";
    private static final String FINISHED_PATH = REPORT_PATH + "/finished";

    @Inject
    JobFarmer(WorkerFinder workerFinder, WorkerControlFactory workerControlFactory,
        TaskPluginRegistry taskPluginRegistry, @Ipc ObjectReader objectReader,
        @Ipc ObjectWriter objectWriter) {
        this.workerFinder = workerFinder;
        this.workerControlFactory = workerControlFactory;
        this.taskPluginRegistry = taskPluginRegistry;
        this.objectReader = objectReader;
        this.objectWriter = objectWriter;
    }

    /**
     * Farm out a job to the available workers.
     *
     * @param job The job to cultivate
     * @return 202 on success with Job in the body, 412 on failure
     */
    public synchronized Response submitJob(Job job) {
        JobStatus jobStatus = new JobStatus(job);

        // Create a set of workers we can lock
        Set<WorkerMetadata> lockedWorkers = new HashSet<>();
        for (ServiceInstance<WorkerMetadata> instance : workerFinder.getWorkers()) {
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

        List<Partition> partitions;
        try {
            TaskPartitioner taskPartitioner =
                taskPluginRegistry.get(job.getTask().getTaskType()).getControllerComponentFactory(
                    objectReader, job.getTask().getConfigNode()).getTaskPartitioner();

            partitions = taskPartitioner
                .partition(job.getJobId(), lockedWorkers.size(), getProgressUrl(job.getJobId()),
                    getFinishedUrl(job.getJobId()), objectReader, job.getTask().getConfigNode(), objectWriter);
        } catch (IOException e) {
            logger.warn("Failed to partition job", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (partitions.isEmpty()) {
            logger.warn("No partitions created");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("No partitions created").build();
        }

        TandemIterator titerator = new TandemIterator(partitions.iterator(), lockedWorkers.iterator());

        // Submit the partition to the worker
        while (titerator.hasNext()) {
            TandemIterator.Pair pair = titerator.next();
            workerControlFactory.getWorkerControl(pair.workerMetadata).submitPartition(job.getJobId(), pair.partition);
            jobStatus.addPartitionStatus(new PartitionStatus(pair.partition, pair.workerMetadata));
        }

        // Save the JobStatus for our accounting
        jobStatus.setFullyPartitioned();
        jobs.put(job.getJobId(), jobStatus);

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
     * Handle a completed partition
     *
     * @param jobId                       The jobId that this taskProgressReport is for
     * @param taskPartitionFinishedReport The results data
     * @return ACCEPTED if we handled the taskProgressReport, NOT_FOUND if this farmer doesn't know the given jobId
     */
    public synchronized Response handlePartitionFinishedReport(UUID jobId,
        TaskPartitionFinishedReport taskPartitionFinishedReport) {
        if (!jobs.containsKey(jobId)) {
            logger.warn("Couldn't find job <" + jobId + ">");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        JobStatus jobStatus = jobs.get(jobId);
        PartitionStatus partitionStatus = jobStatus.getPartitionStatus(taskPartitionFinishedReport.getPartitionId());
        logger.info("Partition <" + partitionStatus.getPartition().getPartitionId() + "> finished");
        partitionStatus.setFinished(taskPartitionFinishedReport.getDuration());

        WorkerControl workerControl = workerControlFactory.getWorkerControl(partitionStatus.getWorkerMetadata());
        workerControl.releaseLock(controllerId);

        // Only set the totalDuration of the job when all workers have been started and have finished
        if (jobStatus.isFinished()) {
            Duration totalDuration = new Duration(0);
            for (PartitionStatus ps : jobStatus.getPartitionStatuses().values()) {
                totalDuration = totalDuration.plus(ps.getDuration());
            }
            jobStatus.setFinalDuration(totalDuration);
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
        private final Iterator<Partition> piterator;
        private final Iterator<WorkerMetadata> witerator;

        public TandemIterator(Iterator<Partition> piterator, Iterator<WorkerMetadata> witerator) {
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

        public class Pair {
            final Partition partition;
            final WorkerMetadata workerMetadata;

            public Pair(Partition partition, WorkerMetadata workerMetadata) {
                this.partition = partition;
                this.workerMetadata = workerMetadata;
            }
        }
    }
}
