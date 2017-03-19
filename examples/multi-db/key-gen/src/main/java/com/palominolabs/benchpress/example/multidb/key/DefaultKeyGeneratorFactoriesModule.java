package com.palominolabs.benchpress.example.multidb.key;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactoryFactory;

public final class DefaultKeyGeneratorFactoriesModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<KeyGeneratorFactoryFactory> binder =
            Multibinder.newSetBinder(binder(), KeyGeneratorFactoryFactory.class);
        binder.addBinding().to(WorkerIdThreadIdCounterKeyGeneratorFactoryFactory.class);
        binder.addBinding().to(MD5PrefixedCounterKeyGeneratorFactoryFactory.class);
        binder.addBinding().to(SaltedCounterKeyGeneratorFactoryFactory.class);
    }
}
