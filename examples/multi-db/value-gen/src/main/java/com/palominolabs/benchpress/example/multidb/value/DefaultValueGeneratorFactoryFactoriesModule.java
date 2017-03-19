package com.palominolabs.benchpress.example.multidb.value;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactory;

public final class DefaultValueGeneratorFactoryFactoriesModule extends AbstractModule{
    @Override
    protected void configure() {
        Multibinder<ValueGeneratorFactoryFactory> binder =
            Multibinder.newSetBinder(binder(), ValueGeneratorFactoryFactory.class);
        binder.addBinding().to(ZeroByteArrayValueGeneratorFactoryFactory.class);
        binder.addBinding().to(RandomByteArrayValueGeneratorFactoryFactory.class);
    }
}
