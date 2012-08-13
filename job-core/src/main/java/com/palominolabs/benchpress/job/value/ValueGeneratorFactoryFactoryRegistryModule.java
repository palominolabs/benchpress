package com.palominolabs.benchpress.job.value;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public final class ValueGeneratorFactoryFactoryRegistryModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(ValueGeneratorFactoryFactoryRegistry.class);
        Multibinder.newSetBinder(binder(), ValueGeneratorFactoryFactory.class);
    }
}
