package com.palominolabs.benchpress.example.sumofsquares;

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Config for an individual worker
 */
class SumOfSquaresWorkerConfig(@param:JsonProperty("start") @field:JsonProperty("start") val first: Long,
                               @param:JsonProperty("end") @field:JsonProperty("end") val last: Long)
