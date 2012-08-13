package com.palominolabs.benchpress.job.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.palominolabs.benchpress.job.task.TaskOperation;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

import javax.annotation.concurrent.Immutable;
import java.util.Map;

@Immutable
public final class Task {
    private final String taskType;

    private final Map<String, Object> config;

    private final TaskOperation taskOperation;

    private final int numThreads;

    private final int numQuanta;

    private final int batchSize;

    private final KeyGen keyGen;

    private final ValueGen valueGen;

    private final int progressReportInterval;

    @JsonCreator
    public Task(@JsonProperty("type") String taskType,
                @JsonProperty("config") Map<String, Object> config,
                @JsonProperty("op") TaskOperation taskOperation,
                @JsonProperty("threads") int numThreads,
                @JsonProperty("quanta") int numQuanta,
                @JsonProperty("batchSize") int batchSize,
                @JsonProperty("keyGen") KeyGen keyGen,
                @JsonProperty("valueGen") ValueGen valueGen,
                @JsonProperty("progressReportInterval") int progressReportInterval) {
        this.taskType = taskType;
        this.progressReportInterval = progressReportInterval;
        this.config = new ImmutableMap.Builder<String, Object>().putAll(config).build();
        this.taskOperation = taskOperation;
        this.numThreads = numThreads;
        this.numQuanta = numQuanta;
        this.batchSize = batchSize;
        this.keyGen = keyGen;
        this.valueGen = valueGen;
    }

    /**
     * @param task      base task
     * @param numQuanta new numQuanta
     */
    public Task(Task task, int numQuanta) {
        this(task.taskType, task.config, task.taskOperation, task.numThreads,
            numQuanta, task.batchSize, task.keyGen, task.valueGen, task.progressReportInterval);
    }

    public Configuration getConfig() {
        return new MapConfiguration(this.config);
    }

    @JsonProperty("batchSize")
    public int getBatchSize() {
        return batchSize;
    }

    @JsonProperty("config")
    public Map<String, Object> getJsonConfig() {
        return this.config;
    }

    @JsonProperty("keyGen")
    public KeyGen getKeyGen() {
        return keyGen;
    }

    @JsonProperty("quanta")
    public int getNumQuanta() {
        return numQuanta;
    }

    @JsonProperty("threads")
    public int getNumThreads() {
        return numThreads;
    }

    @JsonProperty("op")
    public TaskOperation getTaskOperation() {
        return taskOperation;
    }

    @JsonProperty("type")
    public String getTaskType() {
        return taskType;
    }

    @JsonProperty("valueGen")
    public ValueGen getValueGen() {
        return valueGen;
    }

    @JsonProperty("progressReportInterval")
    public int getProgressReportInterval() {
        return progressReportInterval;
    }
}
