package com.palominolabs.benchpress.task.simplehttp;

import com.palominolabs.benchpress.job.task.TaskOutputProcessor;

import java.util.ArrayList;
import java.util.List;

public final class SimpleHttpTaskOutputProcessor implements TaskOutputProcessor {

    public static final SimpleHttpTaskOutputProcessor INSTANCE = new SimpleHttpTaskOutputProcessor();

    private final List<Object> objects = new ArrayList<>();

    private SimpleHttpTaskOutputProcessor() {
    }

    @Override
    public synchronized void handleOutput(Object output) {
        objects.add(output);
    }

    public synchronized List<Object> getObjects() {
        return new ArrayList<>(objects);
    }
}
