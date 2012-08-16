package com.palominolabs.benchpress.job.value;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Used to generate the per-thread ValueGenerator instances. Contains the configuration extracted by {@link
 * ValueGeneratorFactoryFactory}. One ValueGeneratorFactory is used for a partition of a job.
 */
@NotThreadSafe
public interface ValueGeneratorFactory {
    ValueGenerator getValueGenerator();
}
