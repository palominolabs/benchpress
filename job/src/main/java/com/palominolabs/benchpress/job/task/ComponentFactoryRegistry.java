package com.palominolabs.benchpress.job.task;

import com.google.inject.Inject;
import com.palominolabs.benchpress.job.id.IdRegistry;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;

@ThreadSafe
public final class ComponentFactoryRegistry extends IdRegistry<ComponentFactory> {
    @Inject
    ComponentFactoryRegistry(Set<ComponentFactory> componentFactories) {
        super(componentFactories);
    }
}
