package com.palominolabs.benchpress.example.multidb.key;

import com.palominolabs.benchpress.job.id.Id;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.configuration.Configuration;

import java.math.BigInteger;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Generates keys using the partition ID and task counter to produce a string
 * containing a number. The string is hashed with MD5 and the digest is
 * prepended to number. This simulates a common use case where string keys are
 * hashed for good dispersion in ordered partitioned datastores (e.g. HBase).
 * We use keys derived from counters so a group of readers can easily generate
 * keys recently or concurrently used by a group of writers.
 * <p>
 * Setting the config setting "usePartition" to false will ignore the partition
 * ID thereby producing many collisions as each worker's counter is private and
 * initialized to 0.
 */
@ThreadSafe
@Id("MD5_PREFIXED_COUNTER")
final class MD5PrefixedCounterKeyGeneratorFactoryFactory implements KeyGeneratorFactoryFactory {

    @Override
    public KeyGeneratorFactory getKeyGeneratorFactory(Configuration c) {
        return new GeneratorFactory(c.getBoolean("usePartition", true));
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

    @NotThreadSafe
    private static final class Generator implements KeyGenerator {
        private final MessageDigest md;
        private final boolean usePartitionId;

        Generator(boolean usePartitionId) {
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            this.usePartitionId = usePartitionId;
        }

        @Override
        public void writeKey(CharBuffer buf, UUID workerId, long threadId, int partitionId, int counter) {
            long key = counter;
            if (usePartitionId) {
               key |= (long)partitionId << Integer.SIZE;
            }
            String keyString = Long.toString(key);
            byte[] digest = md.digest(keyString.getBytes());
            // Flip the key to randomize
            buf.append(String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)));
            buf.append('|');
            buf.append(keyString);
        }
    }

}
