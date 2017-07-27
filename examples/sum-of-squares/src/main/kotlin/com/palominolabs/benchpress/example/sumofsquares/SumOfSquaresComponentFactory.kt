package com.palominolabs.benchpress.example.sumofsquares

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.LongNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.palominolabs.benchpress.job.task.ComponentFactory
import com.palominolabs.benchpress.job.task.TaskFactory
import com.palominolabs.benchpress.task.reporting.ScopedProgressClient
import java.util.Collections
import java.util.UUID

class SumOfSquaresComponentFactory(private val config: SumOfSquaresWorkerConfig) : ComponentFactory {
    override fun getTaskFactory(): TaskFactory {
        return object : TaskFactory {
            override fun shutdown() {
                // no op
            }

            override fun getRunnables(jobId: UUID, sliceId: Int, workerId: UUID, progressClient: ScopedProgressClient): MutableCollection<Runnable> {
                return Collections.singleton(Runnable {
                    val sum = (config.first..config.last).fold(0L, { acc, i -> acc + i * i })

                    val node = ObjectNode(JsonNodeFactory.instance)
                    node.set("first", LongNode(config.first))
                    node.set("last", LongNode(config.last))
                    node.set("sum", LongNode(sum))

                    progressClient.reportProgress(node)
                })

            }
        }
    }
}

