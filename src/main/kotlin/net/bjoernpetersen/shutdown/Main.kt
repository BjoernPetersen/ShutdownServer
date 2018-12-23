@file:JvmName("Main")

package net.bjoernpetersen.shutdown

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import mu.KotlinLogging

fun main(args: Array<String>) {
    val instance = DaggerInstance.builder()
        .configModule(ConfigModule())
        .killer(WinKiller())
        .build()
    val vertx = Vertx.vertx()
    vertx.deployVerticle(instance.api, DeploymentOptions()) {
        if (!it.succeeded()) {
            val logger = KotlinLogging.logger {}
            logger.error { "Initialization error: ${it.cause().message}" }
            vertx.close()
            System.exit(1)
        }
    }
}
