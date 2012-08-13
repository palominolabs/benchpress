package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Duration;

public final class TaskProgressReport extends AbstractTaskReport {
    private final Duration duration;
    private final int numQuanta;

    @JsonCreator
    TaskProgressReport(@JsonProperty("partitionId") int partitionId, @JsonProperty("sequenceNum") int reportSequenceNum, @JsonProperty("duration") Duration duration, @JsonProperty("numQuanta") int numQuanta) {
        super(partitionId, reportSequenceNum);
        this.duration = duration;
        this.numQuanta = numQuanta;
    }

    @JsonProperty("duration")
    public Duration getDuration() {
        return duration;
    }

    /**
     * @return the quanta whose work is measured in this report. Can be zero if this is the last report (last == true)
     */
    @JsonProperty("numQuanta")
    public int getNumQuanta() {
        return numQuanta;
    }

    public String toString() {
        return super.toString() + ", duration:" + duration + ", numQuanta:" + numQuanta;
    }
}
