package com.palominolabs.benchpress.worker.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.benchpress.worker.LockStatus;
import com.palominolabs.benchpress.worker.WorkerAdvertiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("worker/control")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public final class ControlResource {
    private static final Logger logger = LoggerFactory.getLogger(ControlResource.class);

    @GuardedBy("this")
    private boolean locked = false;
    @GuardedBy("this")
    @Nullable
    private UUID lockingControllerId = null;

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
    public Response acquireLock(@PathParam("controllerId") UUID controllerId) {
        synchronized (this) {
            Response response = expectLockStatus(false);
            if (response != null) {
                return response;
            }

            logger.info("Lock granted to controllerId <" + controllerId + ">");
            lockingControllerId = controllerId;
            locked = true;
        }

        workerAdvertiser.deAdvertiseAvailability();

        return Response.noContent().build();
    }

    /**
     * @param controllerId the controller acquiring the lock; must be the one who acquired the lock
     * @return 204 on success, 412 on failure
     */
    @POST
    @Path("releaseLock/{controllerId}")
    public synchronized Response releaseLock(@PathParam("controllerId") UUID controllerId) {
        synchronized (this) {
            Response response = expectLockStatus(true);
            if (response != null) {
                return response;
            }

            if (!controllerId.equals(lockingControllerId)) {
                logger.info(
                    "Attempt to unlock with controllerId <" + controllerId + "> but locked by <" + lockingControllerId +
                        ">");
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            logger.info("controllerId <" + controllerId + "> released lock");
            lockingControllerId = null;
            locked = false;
        }

        workerAdvertiser.advertiseAvailability();

        return Response.noContent().build();
    }

    /**
     * @return the JSON LockStatus object
     */
    @GET
    @Path("lockStatus")
    public synchronized LockStatus getLockStatus() {
        return new LockStatus(locked, lockingControllerId);
    }

    /**
     * @param expected expected state of {@code locked}
     * @return Response null if the check succeeds (so no error response needs to be sent back), or an error response if
     *         the check failed
     */
    @Nullable
    private Response expectLockStatus(boolean expected) {
        logger.debug("Expecting lockStatus to be " + expected + " (it is " + locked + ")");

        if (locked != expected) {
            lockingControllerId = null;
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }

        return null;
    }
}
