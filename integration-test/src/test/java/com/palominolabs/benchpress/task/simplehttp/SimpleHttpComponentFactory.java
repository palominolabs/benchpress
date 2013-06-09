package com.palominolabs.benchpress.task.simplehttp;

import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOutputProcessor;
import com.palominolabs.benchpress.job.task.TaskOutputProcessorFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class SimpleHttpComponentFactory implements ComponentFactory {

    private final String url;

    SimpleHttpComponentFactory(String url) {
        this.url = url;
    }

    @Nonnull
    @Override
    public TaskFactory getTaskFactory() {
        return new SimpleHttpTaskFactory(url);
    }

    @Nullable
    @Override
    public TaskOutputProcessorFactory getTaskOutputProcessorFactory() {
        return new TaskOutputProcessorFactory() {
            @Nonnull
            @Override
            public TaskOutputProcessor getTaskOutputProcessor() {
                return SimpleHttpTaskOutputProcessor.INSTANCE;
            }
        };
    }
}
