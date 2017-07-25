package com.palominolabs.benchpress.example.multidb.value;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Used to generate the per-thread ValueGenerator instances. Contains the configuration extracted by {@link
 * ValueGeneratorFactoryFactory}. One ValueGeneratorFactory is used for a slice of a job.
 */
@NotThreadSafe
public interface ValueGeneratorFactory {
    ValueGenerator getValueGenerator();
}
