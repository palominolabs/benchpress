package com.palominolabs.benchpress.example.multidb.value;

import com.palominolabs.benchpress.job.id.Id;
import org.apache.commons.configuration.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Used to construct ValueGeneratorFactory instances. A ValueGeneratorFactory contains specific configuration data,
 * whereas ValueGeneratorFactoryFactory is most likely stateless and only exists to encapsulate config parsing logic.
 * Bind implementations with {@link Id}
 */
@ThreadSafe
public interface ValueGeneratorFactoryFactory {

    @Nonnull
    ValueGeneratorFactory getFactory(Configuration c);
}
