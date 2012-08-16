package com.palominolabs.benchpress.worker;

import com.fasterxml.jackson.databind.ObjectReader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.ipc.Ipc;
import com.palominolabs.benchpress.job.json.Partition;
import com.palominolabs.benchpress.job.json.Task;
import com.palominolabs.benchpress.job.registry.JobRegistry;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskFactoryFactory;
import com.palominolabs.benchpress.task.TaskFactoryFactoryRegistry;
import com.palominolabs.benchpress.task.reporting.TaskProgressClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.palominolabs.benchpress.logging.MdcKeys.JOB_ID;

@Singleton
@ThreadSafe
public final class PartitionRunner {
    private static final Logger logger = LoggerFactory.getLogger(PartitionRunner.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executorService);

    /**
     * TODO figure out a good way to worker-scope a random uuid -- @WorkerId binding annotation perhaps? Wrapper class?
     */
    private final UUID workerId = UUID.randomUUID();

    private final TaskProgressClient taskProgressClient;

    private final JobRegistry jobRegistry;

    private final TaskFactoryFactoryRegistry taskFactoryFactoryRegistry;

    private final ObjectReader objectReader;

    @Inject
    PartitionRunner(TaskProgressClient taskProgressClient, JobRegistry jobRegistry,
        TaskFactoryFactoryRegistry taskFactoryFactoryRegistry, @Ipc ObjectReader objectReader) {
        this.taskProgressClient = taskProgressClient;
        this.jobRegistry = jobRegistry;
        this.taskFactoryFactoryRegistry = taskFactoryFactoryRegistry;
        this.objectReader = objectReader;

        // TODO lifecycle would be nice for this
        completionService.submit(new ThreadExceptionWatcherRunnable(completionService), null);
        // TODO also nice to hook into lifecycle...
        logger.info("Worker ID is " + workerId);
    }

    public boolean runPartition(Partition partition) {
        TaskFactory tf;
        try {
            tf = getTaskFactory(partition);
        } catch (IOException e) {
            logger.warn("Couldn't create task factory", e);
            return false;
        }

        AtomicInteger reportSequenceCounter = new AtomicInteger();

        jobRegistry.storeJob(partition.getJobId(), partition.getProgressUrl(), partition.getFinishedUrl());

        HashSet<Future<Void>> futures = new HashSet<Future<Void>>();

        Collection<Runnable> runnables;
        try {
            runnables =
                tf.getRunnables(
                    workerId, partition.getPartitionId(), taskProgressClient, partition.getJobId(),
                    reportSequenceCounter);
        } catch (IOException e) {
            logger.warn("Couldn't make runnables", e);
            return false;
        }

        for (Runnable runnable : runnables) {
            futures.add(completionService.submit(runnable, null));
        }

        completionService.submit(
            new TaskThreadWatcher(futures, partition.getPartitionId(), partition.getJobId(), taskProgressClient,
                jobRegistry, reportSequenceCounter), null);

        return true;
    }

    @Nonnull
    private TaskFactory getTaskFactory(Partition partition) throws IOException {
        Task t = partition.getTask();

        TaskFactoryFactory taskFactoryFactory = taskFactoryFactoryRegistry.get(t.getTaskType());

        return taskFactoryFactory.getTaskFactory(objectReader, t.getConfigNode());
    }

    private static class ThreadExceptionWatcherRunnable implements Runnable {

        private final CompletionService<Void> completionService;

        private ThreadExceptionWatcherRunnable(CompletionService<Void> completionService) {
            this.completionService = completionService;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Future<Void> f;
                    try {
                        f = completionService.take();
                    } catch (InterruptedException e) {
                        logger.info("Interrupted; exiting");
                        return;
                    }

                    try {
                        f.get();
                    } catch (InterruptedException e) {
                        logger.info("Interrupted; exiting");
                        return;
                    } catch (ExecutionException e) {
                        logger.warn("Task execution failed", e);
                    }
                }
            } catch (Throwable t) {
                logger.error("Thread watcher died!", t);
            }
        }
    }

    private static class TaskThreadWatcher implements Runnable {

        private static final Logger watcherLogger = LoggerFactory.getLogger(TaskThreadWatcher.class);
        private final Set<Future<Void>> futures;
        private final int partitionId;
        private final UUID jobId;
        private final TaskProgressClient taskProgressClient;
        private final JobRegistry jobRegistry;
        private final AtomicInteger reportSequenceCounter;

        private TaskThreadWatcher(Set<Future<Void>> futures, int partitionId, UUID jobId,
            TaskProgressClient taskProgressClient, JobRegistry jobRegistry, AtomicInteger reportSequenceCounter) {
            this.futures = futures;
            this.partitionId = partitionId;
            this.jobId = jobId;
            this.taskProgressClient = taskProgressClient;
            this.jobRegistry = jobRegistry;
            this.reportSequenceCounter = reportSequenceCounter;
        }

        @Override
        public void run() {
            MDC.put(JOB_ID, jobId.toString());
            try {

                while (!futures.isEmpty()) {
                    Iterator<Future<Void>> i = futures.iterator();
                    while (i.hasNext()) {
                        Future<Void> f = i.next();
                        // TODO we have no need for real time. We just want to not get stuck behind one infinite loop task.
                        // A fancier way to do this that blocks cleverly would be to create a CompletionService impl that
                        // wraps another CompletionService.
                        try {
                            f.get(1, TimeUnit.SECONDS);
                            watcherLogger.info("Task thread completed ok");
                            i.remove();
                        } catch (InterruptedException e) {
                            watcherLogger.info("Interrupted; exiting");
                            return;
                        } catch (ExecutionException e) {
                            watcherLogger.warn("Task thread execution failed", e);
                            i.remove();
                        } catch (TimeoutException e) {
                            // no op, on to the next
                        }
                    }
                }

                taskProgressClient.reportFinished(jobId, partitionId, reportSequenceCounter.getAndIncrement());
                jobRegistry.removeJob(jobId);

                watcherLogger.info("All task threads finished");
            } finally {
                MDC.remove(JOB_ID);
            }
        }
    }
}
