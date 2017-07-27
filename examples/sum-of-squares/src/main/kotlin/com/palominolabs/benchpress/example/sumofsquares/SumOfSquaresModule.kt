package com.palominolabs.benchpress.example.sumofsquares

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import com.palominolabs.benchpress.job.task.JobTypePlugin

class SumOfSquaresModule : AbstractModule() {
    override fun configure() {
        Multibinder.newSetBinder(binder(), JobTypePlugin::class.java).addBinding().to(SumOfSquaresPlugin::class.java)
    }
}
