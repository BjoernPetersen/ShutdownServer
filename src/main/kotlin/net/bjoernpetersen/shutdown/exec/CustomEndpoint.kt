package net.bjoernpetersen.shutdown.exec

import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

data class CustomEndpoint(
    val get: List<CustomAction>?,
    val put: List<CustomAction>?,
    val post: List<CustomAction>?,
    val delete: List<CustomAction>?)

fun customEndpoint(endpointFile: File): CustomEndpoint {
    return CustomAction.MAPPER.readValue(endpointFile)
}
