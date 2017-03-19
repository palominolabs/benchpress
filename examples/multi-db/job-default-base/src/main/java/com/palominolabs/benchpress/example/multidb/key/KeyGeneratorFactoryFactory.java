package com.palominolabs.benchpress.example.multidb.key;

import com.palominolabs.benchpress.job.id.Id;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.configuration.Configuration;

/**
 * A placeholder class to provide parity with the ValueGenerator class hierarchy. Bind implementations with {@link Id}.
 */
@ThreadSafe
public interface KeyGeneratorFactoryFactory {
    KeyGeneratorFactory getKeyGeneratorFactory(Configuration c);
}
