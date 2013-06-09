package com.palominolabs.benchpress.task.simplehttp;

import com.palominolabs.benchpress.job.task.TaskOutputProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class SimpleHttpTaskOutputProcessor implements TaskOutputProcessor {

    public static final SimpleHttpTaskOutputProcessor INSTANCE = new SimpleHttpTaskOutputProcessor();

    private final List<Object> objects = new ArrayList<>();
    private final AtomicInteger atomicInteger = new AtomicInteger();

    private SimpleHttpTaskOutputProcessor() {
    }

    @Override
    public synchronized void handleOutput(Object output) {
        objects.add(output);
    }

    @Override
    public void close() {
        atomicInteger.incrementAndGet();
    }

    public synchronized List<Object> getObjects() {
        return new ArrayList<>(objects);
    }

    public int getCloseCount() {
        return atomicInteger.get();
    }
}
