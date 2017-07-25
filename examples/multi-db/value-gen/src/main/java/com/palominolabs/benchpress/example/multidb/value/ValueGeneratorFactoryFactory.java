package com.palominolabs.benchpress.example.multidb.value;

import com.palominolabs.benchpress.job.id.Identifiable;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.configuration.Configuration;

/**
 * Used to construct ValueGeneratorFactory instances. A ValueGeneratorFactory contains specific configuration data,
 * whereas ValueGeneratorFactoryFactory is most likely stateless and only exists to encapsulate config parsing logic.
 */
@ThreadSafe
public interface ValueGeneratorFactoryFactory extends Identifiable {

    @Nonnull
    ValueGeneratorFactory getFactory(Configuration c);
}
