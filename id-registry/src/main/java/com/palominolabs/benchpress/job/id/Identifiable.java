package com.palominolabs.benchpress.job.id;

import javax.annotation.Nonnull;

public interface Identifiable {

    /**
     * @return the id to use to store this in a registry. Id must be unique among all plugins that are currently
     * enabled, so using a reverse domain or similar is suggested.
     */
    @Nonnull
    String getRegistryId();
}
