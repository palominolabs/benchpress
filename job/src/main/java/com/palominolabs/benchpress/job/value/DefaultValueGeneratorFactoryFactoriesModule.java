package com.palominolabs.benchpress.job.value;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public final class DefaultValueGeneratorFactoryFactoriesModule extends AbstractModule{
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ValueGeneratorFactoryFactory.class).addBinding()
            .to(ZeroByteArrayValueGeneratorFactoryFactory.class);
    }
}
