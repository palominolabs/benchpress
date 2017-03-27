package com.palominolabs.benchpress.example.sumofsquares;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.palominolabs.benchpress.job.json.Task;
import com.palominolabs.benchpress.job.task.ComponentFactory;
import com.palominolabs.benchpress.job.task.ControllerComponentFactory;
import com.palominolabs.benchpress.job.task.JobSlicer;
import com.palominolabs.benchpress.job.task.JobTypePlugin;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SumOfSquaresPlugin implements JobTypePlugin {

    private static final String TASK_TYPE = "com.palominolabs.benchpress.example.sum-of-squares";

    @Nonnull
    @Override
    public String getRegistryId() {
        return TASK_TYPE;
    }

    @Nonnull
    @Override
    public ComponentFactory getComponentFactory(ObjectReader objectReader, JsonNode configNode) throws IOException {

        SumOfSquaresWorkerConfig config = objectReader.forType(SumOfSquaresWorkerConfig.class).readValue(configNode);

        return new SumOfSquaresComponentFactory(config);
    }

    @Nonnull
    @Override
    public ControllerComponentFactory getControllerComponentFactory(ObjectReader objectReader,
            JsonNode configNode) throws IOException {

        SumOfSquaresJobConfig config = objectReader.forType(SumOfSquaresJobConfig.class).readValue(configNode);

        return new ControllerComponentFactory() {
            @Nonnull
            @Override
            public JobSlicer getJobSlicer() {
                return new JobSlicer() {
                    @Nonnull
                    @Override
                    public List<Task> slice(UUID jobId, int workers, String progressUrl, String finishedUrl, ObjectReader objectReader, JsonNode configNode, ObjectWriter objectWriter) throws IOException {
                        int totalNumbers = config.end - config.start + 1;
                        int numbersPerWorker = totalNumbers / workers;

                        List<Task> list = new ArrayList<>();
                        for (int i = 0; i < workers; i++) {
                            SumOfSquaresWorkerConfig workerConfig =
                                    new SumOfSquaresWorkerConfig(i * numbersPerWorker, i * (numbersPerWorker + 1) - 1);

                            // TODO don't make every plugin implementation do all this json work or boring parameter
                            // hand-off into the Partition. The only interesting thing here is the config that gets
                            // put in the Task; everything else is just mechanical stuff that could be handled
                            // outside the plugin.

                            TokenBuffer tokBuf = new TokenBuffer(objectReader, false);
                            objectWriter.writeValue(tokBuf, workerConfig);
                            JsonParser jp = tokBuf.asParser();
                            JsonNode newJsonNode =
                                    objectReader.readValue(jp, JsonNode.class);
                            jp.close();

                            list.add(new Task(TASK_TYPE, newJsonNode));
                        }

                        return list;                    }
                };
            }

        };
    }
}
