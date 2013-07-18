package com.palominolabs.benchpress.job.key;

import com.palominolabs.benchpress.job.id.Id;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import java.math.BigInteger;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@ThreadSafe
@Id("MD5_PREFIXED_COUNTER")
final class MD5PrefixedCounterKeyGeneratorFactoryFactory implements KeyGeneratorFactoryFactory {

    @Override
    public KeyGeneratorFactory getKeyGeneratorFactory() {
        return new GeneratorFactory();
    }

    @ThreadSafe
    private static final class GeneratorFactory implements KeyGeneratorFactory {
        @Override
        public KeyGenerator getKeyGenerator() {
            return new Generator();
        }
    }

    @NotThreadSafe
    private static final class Generator implements KeyGenerator {
        MessageDigest md;

        Generator() {
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void writeKey(CharBuffer buf, UUID workerId, long threadId, int partitionId, long counter) {
            String key = Long.toString(counter);
            byte[] digest = md.digest(key.getBytes());
            // Flip the key to randomize
            buf.append(String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)));
            buf.append('-');
            buf.append(key);
        }
    }

}
