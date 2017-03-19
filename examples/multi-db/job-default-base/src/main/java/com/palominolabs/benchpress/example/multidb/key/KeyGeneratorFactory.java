package com.palominolabs.benchpress.example.multidb.key;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Used to generate the per-thread KeyGenerator instances. Contains the configuration extracted by {@link
 * KeyGeneratorFactoryFactory}. One KeyGeneratorFactory is used for a partition of a job.
 */
@ThreadSafe
public interface KeyGeneratorFactory {
    /**
     * Should be called by each worker thread.
     *
     * @return a KeyGenerator
     */
    KeyGenerator getKeyGenerator();
}
