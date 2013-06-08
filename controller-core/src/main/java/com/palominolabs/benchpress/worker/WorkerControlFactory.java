package com.palominolabs.benchpress.worker;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.palominolabs.benchpress.ipc.Ipc;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.Map;

@ThreadSafe
public final class WorkerControlFactory {
    private final ObjectWriter objectWriter;
    private final ObjectReader objectReader;

    private final DefaultHttpClient httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());
    private final Map<WorkerMetadata, WorkerControl> workerControls = new HashMap<>();

    @Inject
    WorkerControlFactory(@Ipc ObjectWriter objectWriter, @Ipc ObjectReader objectReader) {
        this.objectWriter = objectWriter;
        this.objectReader = objectReader;
    }

    public synchronized WorkerControl getWorkerControl(WorkerMetadata workerMetadata) {
        WorkerControl wc = workerControls.get(workerMetadata);
        if (wc == null) {
            wc = new WorkerControl(workerMetadata, httpClient, objectWriter, objectReader);
            workerControls.put(workerMetadata, wc);
        }
        return wc;
    }
}
