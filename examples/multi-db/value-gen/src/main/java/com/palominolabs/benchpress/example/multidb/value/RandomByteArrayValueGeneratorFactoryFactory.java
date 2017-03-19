package com.palominolabs.benchpress.example.multidb.value;

import com.palominolabs.benchpress.example.multidb.value.ValueGenerator;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactory;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactory;
import java.util.Random;

import com.palominolabs.benchpress.job.id.Id;
import org.apache.commons.configuration.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Id("RANDOM_BYTE_ARRAY")
final class RandomByteArrayValueGeneratorFactoryFactory implements ValueGeneratorFactoryFactory {

    @Nonnull
    @Override
    public ValueGeneratorFactory getFactory(Configuration c) {
        return new GeneratorFactory(c.getInt("minSize", 16), c.getInt("maxSize", 1024),
          c.getLong("seed", 42L));
    }

    // Since at least Java 6 Random is thread safe
    @ThreadSafe
    private static final class Generator implements ValueGenerator {
        private final int minSize;
        private final int maxSize;
        private final Random rng;

        Generator(int minSize, int maxSize, long seed) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.rng = new Random(seed);
        }

        @Override
        public byte[] getValue() {
            int size = minSize;
            // Choose a random size if not configured for a fixed size
            if (minSize != maxSize) {
                size = minSize + rng.nextInt(maxSize - minSize);
            }
            byte[] result = new byte[size];
            rng.nextBytes(result);
            return result;
        }
    }

    @ThreadSafe
    private static final class GeneratorFactory implements ValueGeneratorFactory {
        private final int minSize;
        private final int maxSize;
        private final long seed;

        GeneratorFactory(int minSize, int maxSize, long seed) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.seed = seed;
        }

        @Override
        public ValueGenerator getValueGenerator() {
            return new Generator(minSize, maxSize, seed);
        }
    }
}
