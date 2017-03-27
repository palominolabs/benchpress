package com.palominolabs.benchpress.controller.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.controller.JobFarmer;
import com.palominolabs.benchpress.job.JobStatus;
import com.palominolabs.benchpress.job.json.Job;
import com.palominolabs.benchpress.logging.MdcKeys;
import com.palominolabs.benchpress.task.reporting.TaskPartitionFinishedReport;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.MDC;

@Path("controller/job")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public final class ControllerJobResource {
    private JobFarmer jobFarmer;

    @Inject
    ControllerJobResource(JobFarmer jobFarmer) {
        this.jobFarmer = jobFarmer;
    }

    /**
     * @param job The job to start
     * @return 202 and the Job object as JSON, 412 on failure
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response submit(Job job) {
        MDC.put(MdcKeys.JOB_ID, job.getJobId().toString());

        Response response;
        try {
            response = jobFarmer.submitJob(job);
        } finally {
            MDC.remove(MdcKeys.JOB_ID);
        }
        return response;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response list() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html>\n<body>\n");

        stringBuilder.append("<ul>\n");
        Set<UUID> jobIds = jobFarmer.getJobIds();
        for (UUID jobId : jobIds) {
            stringBuilder.append("<li><a href='/job/").append(jobId).append("'>").append(jobId).append("</a></li>\n");
        }
        stringBuilder.append("</ul>\n");

        stringBuilder.append("</html>");
        return Response.status(Response.Status.OK).entity(stringBuilder.toString()).build();
    }

    @POST
    @Path("{jobId}/report/finished")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reportFinished(@PathParam("jobId") UUID jobId, TaskPartitionFinishedReport taskPartitionFinishedReport) {
        MDC.put(MdcKeys.JOB_ID, jobId.toString());

        Response response;
        try {
            response = jobFarmer.handlePartitionFinishedReport(jobId, taskPartitionFinishedReport);
        } finally {
            MDC.remove(MdcKeys.JOB_ID);
        }
        return response;
    }

    /**
     * Get status of a job
     *
     * @param jobId The job to get status of
     * @return 200 and a Job object
     */
    @GET
    @Path("{jobId}")
    public JobStatus get(@PathParam("jobId") UUID jobId) {
        MDC.put(MdcKeys.JOB_ID, jobId.toString());

        JobStatus job;
        try {
            job = jobFarmer.getJob(jobId);
        } finally {
            MDC.remove(MdcKeys.JOB_ID);
        }
        return job;
    }
}
