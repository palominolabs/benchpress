package com.palominolabs.benchpress.job.key;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public final class DefaultKeyGeneratorFactoriesModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), KeyGeneratorFactoryFactory.class).addBinding()
            .to(WorkerIdThreadIdCounterKeyGeneratorFactoryFactory.class);
    }
}
