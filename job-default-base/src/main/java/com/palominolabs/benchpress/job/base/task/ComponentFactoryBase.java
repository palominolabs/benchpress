package com.palominolabs.benchpress.job.base.task;

import com.palominolabs.benchpress.job.key.KeyGeneratorFactory;
import com.palominolabs.benchpress.job.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactory;
import com.palominolabs.benchpress.job.value.ValueGeneratorFactoryFactoryRegistry;

public abstract class ComponentFactoryBase implements ComponentFactory {

    private final KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry;
    private final ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry;

    protected ComponentFactoryBase(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        this.keyGeneratorFactoryFactoryRegistry = keyGeneratorFactoryFactoryRegistry;
        this.valueGeneratorFactoryFactoryRegistry = valueGeneratorFactoryFactoryRegistry;
    }

    protected KeyGeneratorFactory getKeyGeneratorFactory(TaskConfigBase config) {
        return keyGeneratorFactoryFactoryRegistry.get(config.getKeyGen().keyGenType).getKeyGeneratorFactory();
    }

    protected ValueGeneratorFactory getValueGeneratorFactory(TaskConfigBase config) {
        return valueGeneratorFactoryFactoryRegistry.get(config.getValueGen().getValueGenType())
            .getFactory(config.getValueGen().getConfig());
    }
}
