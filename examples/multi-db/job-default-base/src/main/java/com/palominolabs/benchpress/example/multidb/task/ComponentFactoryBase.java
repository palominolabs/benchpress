package com.palominolabs.benchpress.example.multidb.task;

import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactory;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactoryFactoryRegistry;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactory;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactoryFactoryRegistry;

public abstract class ComponentFactoryBase implements ComponentFactory {

    private final KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry;
    private final ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry;

    protected ComponentFactoryBase(KeyGeneratorFactoryFactoryRegistry keyGeneratorFactoryFactoryRegistry,
        ValueGeneratorFactoryFactoryRegistry valueGeneratorFactoryFactoryRegistry) {
        this.keyGeneratorFactoryFactoryRegistry = keyGeneratorFactoryFactoryRegistry;
        this.valueGeneratorFactoryFactoryRegistry = valueGeneratorFactoryFactoryRegistry;
    }

    protected KeyGeneratorFactory getKeyGeneratorFactory(TaskConfigBase config) {
        return keyGeneratorFactoryFactoryRegistry.get(config.getKeyGen().keyGenType)
            .getKeyGeneratorFactory(config.getKeyGen().getConfig());
    }

    protected ValueGeneratorFactory getValueGeneratorFactory(TaskConfigBase config) {
        return valueGeneratorFactoryFactoryRegistry.get(config.getValueGen().getValueGenType())
            .getFactory(config.getValueGen().getConfig());
    }
}
