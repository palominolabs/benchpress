package com.palominolabs.benchpress.job.registry;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.UUID;

@ThreadSafe
@Singleton
final class MemoryJobRegistry implements JobRegistry {

    private final Map<UUID, JobData> data = Maps.newHashMap();

    @Inject
    MemoryJobRegistry() {
    }

    @Override
    public synchronized void storeJob(UUID jobId, String progressUrl, String finishedUrl) {
        data.put(jobId, new JobData(progressUrl, finishedUrl));
    }

    @Override
    public synchronized String getProgressUrl(UUID jobId) {
        JobData j = data.get(jobId);

        if (j == null) {
            return null;
        }

        return j.progressUrl;
    }

    @Override
    public synchronized String getFinishedUrl(UUID jobId) {
        JobData j = data.get(jobId);

        if (j == null) {
            return null;
        }

        return j.finishedUrl;
    }

    @Override
    public synchronized void removeJob(UUID jobId) {
        data.remove(jobId);
    }

    private static class JobData {
        private final String progressUrl;
        private final String finishedUrl;

        private JobData(String progressUrl, String finishedUrl) {
            this.progressUrl = progressUrl;
            this.finishedUrl = finishedUrl;
        }
    }
}
