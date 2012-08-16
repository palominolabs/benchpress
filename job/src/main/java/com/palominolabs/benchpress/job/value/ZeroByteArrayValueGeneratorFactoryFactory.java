package com.palominolabs.benchpress.job.value;

import com.palominolabs.benchpress.job.id.Id;
import org.apache.commons.configuration.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

// TODO move to separate module
@ThreadSafe
@Id("ZERO_BYTE_ARRAY")
final class ZeroByteArrayValueGeneratorFactoryFactory implements ValueGeneratorFactoryFactory {

    @Nonnull
    @Override
    public ValueGeneratorFactory getFactory(Configuration c) {
        return new GeneratorFactory(c.getInt("size"));
    }

    @NotThreadSafe
    private static final class ZeroByteArrayValueGenerator implements ValueGenerator {

        private byte[] byteArr;

        ZeroByteArrayValueGenerator(int size) {
            byteArr = new byte[size];
        }

        @Override
        public byte[] getValue() {
            return byteArr;
        }
    }

    @NotThreadSafe
    private static final class GeneratorFactory implements ValueGeneratorFactory {
        private final int size;

        GeneratorFactory(int size) {
            this.size = size;
        }

        @Override
        public ValueGenerator getValueGenerator() {
            return new ZeroByteArrayValueGenerator(size);
        }
    }
}
