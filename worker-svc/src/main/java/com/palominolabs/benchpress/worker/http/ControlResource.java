package com.palominolabs.benchpress.worker.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.worker.LockStatus;
import com.palominolabs.benchpress.worker.WorkerAdvertiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Path("worker/control")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public final class ControlResource {
    private final Logger logger = LoggerFactory.getLogger(ControlResource.class);

    // TODO tidy up thread safety
    private final AtomicBoolean locked = new AtomicBoolean(false);
    private final AtomicReference<String> controllerId = new AtomicReference<String>(null);

    private final WorkerAdvertiser workerAdvertiser;

    @Inject
    ControlResource(WorkerAdvertiser workerAdvertiser) {
        this.workerAdvertiser = workerAdvertiser;
    }

    /**
     * @param controllerId the controller acquiring the lock
     * @return 204 on success, 412 on failure
     */
    @POST
    @Path("acquireLock/{controllerId}")
    public synchronized Response acquireLock(@PathParam("controllerId") String controllerId) {
        return swapFrom(false, true, controllerId);
    }

    /**
     * @param controllerId the controller acquiring the lock; must be the one who acquired the lock
     * @return 204 on success, 412 on failure
     */
    @POST
    @Path("releaseLock/{controllerId}")
    public synchronized Response releaseLock(@PathParam("controllerId") String controllerId) {
        return swapFrom(true, false, controllerId);
    }

    /**
     * @return 200 and the JSON LockStatus object
     */
    @GET
    @Path("lockStatus")
    public synchronized LockStatus getLockStatus() {
        return new LockStatus(locked.get(), controllerId.get());
    }

    private Response swapFrom(boolean expect, boolean update, String newControllerId) {
        if (locked.get() && !newControllerId.equals(controllerId.get())) {
            logger.info("Attempt to unlock with controllerId <" + newControllerId + "> but locked by <" + controllerId.get() + ">");
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }

        logger.debug("Expecting lockStatus to be " + expect + " (it is " + locked.get() + ") for update to " + update);
        boolean success = locked.compareAndSet(expect, update);

        if (success) {
            if (locked.get()) {
                logger.info("Lock granted to controllerId <" + newControllerId + ">");
                controllerId.set(newControllerId);
                workerAdvertiser.deAdvertiseAvailability();
            } else {
                logger.info("controllerId <" + newControllerId + "> released lock");
                controllerId.set(null);
                workerAdvertiser.advertiseAvailability();
            }

            return Response.noContent().build();
        }

        controllerId.set(null);
        return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }
}
