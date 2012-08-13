package com.palominolabs.benchpress.job.task;

import com.palominolabs.benchpress.job.key.KeyGeneratorFactory;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactory;
import com.palominolabs.benchpress.task.reporting.TaskProgressClient;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A TaskFactory creates the runnables that actually do the work.
 */
@NotThreadSafe
public interface TaskFactory {

/* TODO a custom TaskFactoryFactory and TaskFactory could, in theory, pretty much ignore all of these parameters. We need
a better way of organizing configuration that makes the common case easier without also creating sometimes-irrelevant boilerplate.
Perhaps create an abstract class that one could inherit from for json config bean, but you could always supply your own?
*/

    /**
     * @param keyGeneratorFactory    used by the runnables to generate keys
     * @param valueGeneratorFactory  used by the runnables to generate values
     * @param taskOperation          the type of work the runnables should do
     * @param numThreads             number of threads that should run concurrently
     * @param numQuanta              total number of items to read, write, etc that each thread should perform
     * @param batchSize              how many operations should be in each batch
     * @param workerId               the worker that these tasks are running in
     * @param partitionId            the partition of the overall job that these tasks are part of
     * @param taskProgressClient     used to report progress back to the controller
     * @param jobId                  job id
     * @param progressReportInterval how many quanta should elapse between reporting progress
     * @param reportSequenceCounter  used to provide a sequence for all reports sent back to the controller
     *                               (partition-scoped)
     * @return runnables
     * @throws IOException
     */
    Collection<Runnable> getRunnables(
        KeyGeneratorFactory keyGeneratorFactory, ValueGeneratorFactory valueGeneratorFactory,
        TaskOperation taskOperation, int numThreads, int numQuanta, int batchSize, UUID workerId, int partitionId,
        TaskProgressClient taskProgressClient, UUID jobId, int progressReportInterval,
        AtomicInteger reportSequenceCounter) throws IOException;

    void shutdown();
}
