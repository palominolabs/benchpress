package com.palominolabs.benchpress.example.sumofsquares

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.util.TokenBuffer
import com.palominolabs.benchpress.job.json.Task
import com.palominolabs.benchpress.job.task.ComponentFactory
import com.palominolabs.benchpress.job.task.ControllerComponentFactory
import com.palominolabs.benchpress.job.task.JobSlicer
import com.palominolabs.benchpress.job.task.JobTypePlugin

class SumOfSquaresPlugin : JobTypePlugin {

    companion object {
        val TASK_TYPE: String = "com.palominolabs.benchpress.example.sum-of-squares"
    }

    override fun getComponentFactory(objectReader: ObjectReader, configNode: JsonNode): ComponentFactory {
        val config: SumOfSquaresWorkerConfig = objectReader.forType(SumOfSquaresWorkerConfig::class.java).readValue(configNode)

        return SumOfSquaresComponentFactory(config)
    }

    override fun getRegistryId(): String = TASK_TYPE

    override fun getControllerComponentFactory(objectReader: ObjectReader, configNode: JsonNode): ControllerComponentFactory {
        val config: SumOfSquaresJobConfig = objectReader.forType(SumOfSquaresJobConfig::class.java).readValue(configNode)

        return ControllerComponentFactory {
            JobSlicer { _, workers, _, _, _, objectWriter ->

                val totalNumbers = config.first - config.last + 1
                val numbersPerWorker = totalNumbers / workers

                val list = mutableListOf<Task>()

                for (i in (0..workers)) {
                    val workerConfig = SumOfSquaresWorkerConfig(i * numbersPerWorker, i * (numbersPerWorker + 1) - 1)

                    // TODO don't make every plugin implementation do all this json work

                    val tokBuf = TokenBuffer(objectReader, false)
                    objectWriter.writeValue(tokBuf, workerConfig)
                    val jp = tokBuf.asParser()
                    val newJsonNode = objectReader.readValue(jp, JsonNode::class.java)
                    jp.close()

                    list.add(Task(TASK_TYPE, newJsonNode))
                }

                list
            }
        }
    }
}
