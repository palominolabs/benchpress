package com.palominolabs.benchpress.worker;

import com.google.inject.Singleton;
import com.palominolabs.benchpress.job.task.TaskOutputProcessor;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;
import com.palominolabs.benchpress.job.task.TaskOutputQueueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Queue provider that uses a single thread for all tasks of the same type in a single job.
 */
@Singleton
@ThreadSafe
final class DefaultTaskOutputQueueProvider implements TaskOutputQueueProvider {
    private static final Logger logger = LoggerFactory.getLogger(DefaultTaskOutputQueueProvider.class);

    static final Object END_OF_QUEUE = new Object();

    private static final int QUEUE_SIZE = 1024;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<UUID, BlockingQueue<Object>> queueMap = new HashMap<>();
    private final Map<UUID, Future<?>> futureMap = new HashMap<>();

    @Nonnull
    @Override
    public synchronized BlockingQueue<Object> getQueue(@Nonnull UUID jobId,
        @Nonnull TaskOutputProcessorFactory taskOutputProcessorFactory) {
        BlockingQueue<Object> blockingQueue = queueMap.get(jobId);
        if (blockingQueue == null) {
            blockingQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
            queueMap.put(jobId, blockingQueue);

            final TaskOutputProcessor taskOutputProcessor = taskOutputProcessorFactory.getTaskOutputProcessor();

            final BlockingQueue<?> finalBlockingQueue = blockingQueue;
            Future<?> future = executorService.submit(new QueueWatcherRunnable(finalBlockingQueue, taskOutputProcessor));

            futureMap.put(jobId, future);
        }

        return blockingQueue;
    }

    @Override
    public synchronized void removeJob(@Nonnull UUID jobId) {
        futureMap.remove(jobId);
        BlockingQueue<Object> queue = queueMap.remove(jobId);
        if (queue != null) {
            if (!queue.offer(END_OF_QUEUE)) {
                logger.warn("Could not add end of queue marker for job " + jobId);
            }
        }
    }

    private static class QueueWatcherRunnable implements Runnable {
        private final BlockingQueue<?> finalBlockingQueue;
        private final TaskOutputProcessor taskOutputProcessor;

        public QueueWatcherRunnable(BlockingQueue<?> finalBlockingQueue, TaskOutputProcessor taskOutputProcessor) {
            this.finalBlockingQueue = finalBlockingQueue;
            this.taskOutputProcessor = taskOutputProcessor;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Object obj = finalBlockingQueue.take();

                    if (obj == END_OF_QUEUE) {
                        logger.debug("Queue end reached; exiting");
                        taskOutputProcessor.close();
                        return;
                    }

                    taskOutputProcessor.handleOutput(obj);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.info("Queue consumer interrupted");
                    taskOutputProcessor.close();
                    break;
                } catch (RuntimeException e) {
                    logger.warn("TaskOutputProcessor failed", e);
                }
            }
        }
    }
}
