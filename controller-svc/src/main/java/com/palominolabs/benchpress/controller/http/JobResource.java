package com.palominolabs.benchpress.controller.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.controller.JobFarmer;
import com.palominolabs.benchpress.job.JobStatus;
import com.palominolabs.benchpress.job.json.Job;
import com.palominolabs.benchpress.logging.MdcKeys;
import com.palominolabs.benchpress.task.reporting.TaskPartitionFinishedReport;
import com.palominolabs.benchpress.task.reporting.TaskProgressReport;
import org.slf4j.MDC;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.UUID;

@Path("job")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public final class JobResource {
    private JobFarmer jobFarmer;

    @Inject
    JobResource(JobFarmer jobFarmer) {
        this.jobFarmer = jobFarmer;
    }

    /**
     * @param job The job to start
     * @return 202 & the Job object as JSON, 412 on failure
     */
    @PUT
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

    /**
     * Where workers submit job results
     *
     * @param jobId The job that these results are for
     * @param taskProgressReport The results object
     * @return 202 on success, 404 on failure
     */
    @POST
    @Path("{jobId}/report/progress")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reportProgress(@PathParam("jobId") UUID jobId, TaskProgressReport taskProgressReport) {
        MDC.put(MdcKeys.JOB_ID, jobId.toString());

        Response response;
        try {
            response = jobFarmer.handleProgressReport(jobId, taskProgressReport);
        } finally {
            MDC.remove(MdcKeys.JOB_ID);
        }
        return response;
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
     * @return 200 & a Job object
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
