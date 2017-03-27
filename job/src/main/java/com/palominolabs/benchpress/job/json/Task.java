package com.palominolabs.benchpress.job.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.palominolabs.benchpress.job.id.Identifiable;
import com.palominolabs.benchpress.job.task.ComponentFactory;

public final class Task {
    private final String taskType;

    private final JsonNode configNode;

    /**
     * @param taskType   task type
     * @param configNode config json. This claims ownership of the node object.
     */
    @JsonCreator
    public Task(@JsonProperty("type") String taskType, @JsonProperty("config") JsonNode configNode) {
        this.taskType = taskType;
        this.configNode = configNode;
    }

    /**
     * Don't mess with this JsonNode; just read from it.
     *
     * @return json node representing the config data. {@link ComponentFactory} implementations should deserialize as
     * they see fit.
     */
    @JsonProperty("config")
    public JsonNode getConfigNode() {
        return this.configNode;
    }

    /**
     * Should match the {@link Identifiable} implementation on a {@link ComponentFactory}
     *
     * @return the task type
     */
    @JsonProperty("type")
    public String getTaskType() {
        return taskType;
    }
}
