package com.palominolabs.benchpress.example.multidb.key;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public final class KeyGeneratorFactoryFactoryRegistryModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(KeyGeneratorFactoryFactoryRegistry.class);
        Multibinder.newSetBinder(binder(), KeyGeneratorFactoryFactory.class);
    }
}
