package com.palominolabs.benchpress.worker.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.job.json.JobSlice;
import com.palominolabs.benchpress.worker.SliceRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * Receives slices sent by the controller.
 */
@Path("worker/job")
@Singleton
public final class WorkerJobResource {

    private static final Logger logger = LoggerFactory.getLogger(WorkerJobResource.class);

    private final SliceRunner sliceRunner;

    @Inject
    WorkerJobResource(SliceRunner sliceRunner) {
        this.sliceRunner = sliceRunner;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{jobId}/slice")
    public Response submit(@PathParam("jobId") UUID jobId, JobSlice jobSlice) {
        logger.info("Processing job submission <" + jobId + "> slice <" + jobSlice.getSliceId() + ">");

        if (!sliceRunner.runSlice(jobSlice)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.status(Response.Status.ACCEPTED).build();
    }

}
