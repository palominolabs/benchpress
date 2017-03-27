package com.palominolabs.benchpress.example.multidb.key;

import java.nio.CharBuffer;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.configuration.Configuration;

/**
 * Generates keys using the partition ID and task counter to produce a string
 * containing a number. The key is then "salted" with a random hexadecimal
 * digit prepended to the number. This simulates a common use case where
 * keys produced from increasing numbers (e.g. timestamps) are salted for good
 * dispersion in ordered partitioned datastores (e.g. HBase).
 * <p>
 * Setting the config setting "usePartition" to false will ignore the partition
 * ID thereby producing many collisions as each worker's counter is private and
 * initialized to 0.
 */
@ThreadSafe
final class SaltedCounterKeyGeneratorFactoryFactory implements KeyGeneratorFactoryFactory {

    @Override
    public KeyGeneratorFactory getKeyGeneratorFactory(Configuration c) {
        return new GeneratorFactory(c.getBoolean("usePartition", true));
    }

    @Nonnull
    @Override
    public String getRegistryId() {
        return "SALTED_COUNTER";
    }

    @ThreadSafe
    private static final class GeneratorFactory implements KeyGeneratorFactory {
        private final boolean usePartitionId;

        GeneratorFactory(boolean usePartitionId) {
            this.usePartitionId = usePartitionId;
        }

        @Override
        public KeyGenerator getKeyGenerator() {
            return new Generator(usePartitionId);
        }
    }

    // Random is threadsafe from at least Java 6
    @ThreadSafe
    private static final class Generator implements KeyGenerator {
        private final Random rng = new Random();
        private final boolean usePartitionId;

        Generator(boolean usePartitionId) {
            this.usePartitionId = usePartitionId;
        }

        @Override
        public void writeKey(CharBuffer buf, UUID workerId, long threadId, int partitionId, int counter) {
            long key = counter;
            if (usePartitionId) {
               key |= (long)partitionId << Integer.SIZE;
            }
            String keyString = Long.toString(key);
            // Salt the key with a random hex digit to disperse the key series 16 ways
            buf.append(String.format("%X", rng.nextInt(16)));
            buf.append(keyString);
        }
    }

}
