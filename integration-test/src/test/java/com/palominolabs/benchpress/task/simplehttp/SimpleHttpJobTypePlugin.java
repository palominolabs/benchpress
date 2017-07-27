package com.palominolabs.benchpress.task.simplehttp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.palominolabs.benchpress.job.json.Task;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.ControllerComponentFactory;
import com.palominolabs.benchpress.job.task.JobSlicer;
import com.palominolabs.benchpress.job.task.JobTypePlugin;
import com.palominolabs.benchpress.job.task.TaskFactory;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

public final class SimpleHttpJobTypePlugin implements JobTypePlugin {

    public static final String TASK_TYPE = "simple-http";

    @Inject
    SimpleHttpJobTypePlugin() {
    }

    @Nonnull
    @Override
    public ComponentFactory getComponentFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {
        ObjectNode obj = objectReader.forType(ObjectNode.class).readValue(configNode);
        String url = obj.get("url").textValue();

        return new ComponentFactory() {
            @Nonnull
            @Override
            public TaskFactory getTaskFactory() {
                return new SimpleHttpTaskFactory(url);
            }
        };
    }

    @Nonnull
    @Override
    public ControllerComponentFactory getControllerComponentFactory(ObjectReader objectReader,
            JsonNode configNode) throws IOException {
        return new ControllerComponentFactory() {
            @Nonnull
            @Override
            public JobSlicer getJobSlicer() {
                return new JobSlicer() {
                    @Nonnull
                    @Override
                    public List<Task> slice(UUID jobId, int workers, String progressUrl, String finishedUrl,
                                            ObjectReader objectReader, ObjectWriter objectWriter) throws IOException {
                        return Lists.newArrayList(new Task(TASK_TYPE, configNode));
                    }
                };
            }
        };
    }

    @Nonnull
    @Override
    public String getRegistryId() {
        return TASK_TYPE;
    }
}
