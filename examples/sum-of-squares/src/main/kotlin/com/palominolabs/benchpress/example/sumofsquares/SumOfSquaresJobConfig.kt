package com.palominolabs.benchpress.example.sumofsquares

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Config for the overall job, different from [SumOfSquaresWorkerConfig] to show that they need not be the same.
 */
class SumOfSquaresJobConfig(@param:JsonProperty("start") @field:JsonProperty("start") val first: Long,
                            @param:JsonProperty("end") @field:JsonProperty("end") val last: Long)
