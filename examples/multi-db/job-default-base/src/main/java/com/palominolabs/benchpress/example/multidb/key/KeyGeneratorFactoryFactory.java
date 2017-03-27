package com.palominolabs.benchpress.example.multidb.key;

import com.palominolabs.benchpress.job.id.Identifiable;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.configuration.Configuration;

/**
 * A placeholder class to provide parity with the ValueGenerator class hierarchy.
 */
@ThreadSafe
public interface KeyGeneratorFactoryFactory extends Identifiable {
    KeyGeneratorFactory getKeyGeneratorFactory(Configuration c);
}
