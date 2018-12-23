@file:JvmName("Main")

package net.bjoernpetersen.shutdown

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import net.bjoernpetersen.shutdown.api.Api

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(Api(readConfig()), DeploymentOptions())
}
