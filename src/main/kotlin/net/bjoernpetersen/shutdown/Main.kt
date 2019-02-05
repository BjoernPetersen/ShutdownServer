@file:JvmName("Main")

package net.bjoernpetersen.shutdown

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import mu.KotlinLogging
import net.bjoernpetersen.shutdown.exec.KillerModule

fun main() {
    val instance = DaggerInstance.builder()
        .configModule(ConfigModule())
        .killerModule(KillerModule())
        .build()
    val vertx = Vertx.vertx()
    vertx.deployVerticle(instance.api, DeploymentOptions()) {
        val logger = KotlinLogging.logger {}
        if (it.succeeded()) {
            logger.info { "Running..." }
        } else {
            logger.error { "Initialization error: ${it.cause().message}" }
            vertx.close()
            System.exit(1)
        }
    }
}
