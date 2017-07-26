package com.palominolabs.benchpress.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.palominolabs.benchpress.job.json.Job;
import com.palominolabs.benchpress.worker.WorkerMetadata;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
final class JobStatus {
    private final Job job;

    @GuardedBy("this")
    private final List<SliceMetadata> slices;

    JobStatus(Job job, List<SliceMetadata> slices) {
        this.job = job;
        this.slices = slices;
    }

    synchronized JobStatusResponse buildStatusResponse() {

        List<SliceStatusResponse> sliceResponses = slices.stream()
                .map((metadata) -> new SliceStatusResponse(metadata.getTask(), metadata.getWorkerMetadata(),
                        metadata.isFinished(),
                        metadata.getDuration(), metadata.getProgress())).collect(Collectors.toList());

        return new JobStatusResponse(job, sliceResponses, isFinished(), totalDuration());
    }

    synchronized void sliceFinished(int sliceId, @Nonnull Duration duration) {
        slices.get(sliceId).markFinished(duration);
    }

    synchronized void sliceProgress(int sliceId, @Nonnull JsonNode data) {
        slices.get(sliceId).markProgress(data);
    }

    synchronized WorkerMetadata getWorkerMetadata(int sliceId) {
        return slices.get(sliceId).getWorkerMetadata();
    }

    private boolean isFinished() {
        for (SliceMetadata sliceStatus : slices) {
            if (!sliceStatus.isFinished()) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private Duration totalDuration() {
        Duration totalDuration = Duration.ZERO;

        for (SliceMetadata sliceStatus : slices) {
            Duration sliceDuration = sliceStatus.getDuration();
            if (sliceDuration == null) {
                return null;
            }

            totalDuration = totalDuration.plus(sliceDuration);
        }

        return totalDuration;
    }
}
