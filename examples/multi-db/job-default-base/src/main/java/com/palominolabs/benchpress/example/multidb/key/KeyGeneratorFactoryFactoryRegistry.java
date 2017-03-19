package com.palominolabs.benchpress.example.multidb.key;

import com.google.inject.Inject;
import com.palominolabs.benchpress.job.id.IdRegistry;

import java.util.Set;

public final class KeyGeneratorFactoryFactoryRegistry extends IdRegistry<KeyGeneratorFactoryFactory> {

    @Inject
    KeyGeneratorFactoryFactoryRegistry(Set<KeyGeneratorFactoryFactory> instances) {
        super(instances);
    }
}
