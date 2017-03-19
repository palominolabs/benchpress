package com.palominolabs.benchpress.example.multidb.value;

import com.google.inject.Inject;
import com.palominolabs.benchpress.job.id.IdRegistry;

import java.util.Set;

public final class ValueGeneratorFactoryFactoryRegistry extends IdRegistry<ValueGeneratorFactoryFactory> {

    @Inject
    ValueGeneratorFactoryFactoryRegistry(Set<ValueGeneratorFactoryFactory> instances) {
        super(instances);
    }
}
