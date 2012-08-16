package com.palominolabs.benchpress.job.key;

import com.palominolabs.benchpress.job.id.Id;

import javax.annotation.concurrent.ThreadSafe;
import java.nio.CharBuffer;
import java.util.UUID;

// TODO move to separate module
@ThreadSafe
@Id("WORKER_ID_THREAD_ID_COUNTER")
final class WorkerIdThreadIdCounterKeyGeneratorFactoryFactory implements KeyGeneratorFactoryFactory {

    private static final Factory FACTORY = new Factory();

    @Override
    public KeyGeneratorFactory getKeyGeneratorFactory() {
        return FACTORY;
    }

    @ThreadSafe
    private static class Factory implements KeyGeneratorFactory {
        private static KeyGenerator GENERATOR = new Generator();

        @Override
        public KeyGenerator getKeyGenerator() {
            return GENERATOR;
        }
    }

    @ThreadSafe
    private static class Generator implements KeyGenerator {
        @Override
        public void writeKey(CharBuffer buf, UUID workerId, long threadId, int partitionId, int counter) {
            buf.append(workerId.toString());
            buf.append('|').append(Long.toString(threadId)).append('|').append(Integer.toString(partitionId))
                .append('|')
                .append(Integer.toString(counter));
        }
    }
}
