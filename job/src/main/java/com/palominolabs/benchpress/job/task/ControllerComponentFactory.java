package com.palominolabs.benchpress.job.task;

import javax.annotation.Nonnull;

/**
 * Provides components used by the controller.
 */
public interface ControllerComponentFactory {

    @Nonnull
    JobSlicer getJobSlicer();
}
