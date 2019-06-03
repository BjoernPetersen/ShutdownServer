@file:JvmName("Main")

package net.bjoernpetersen.shutdown

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import mu.KotlinLogging
import net.bjoernpetersen.shutdown.exec.KillerModule
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val instance = DaggerInstance.builder()
        .argsModule(ArgsModule(args))
        .configModule(ConfigModule())
        .killerModule(KillerModule())
        .build()
    val vertx = Vertx.vertx()
    vertx.deployVerticle(instance.api, DeploymentOptions()) {
        val logger = KotlinLogging.logger {}
        if (it.succeeded()) {
            logger.info { "Running..." }
        } else {
            logger.error(it.cause()) {}
            vertx.close()
            exitProcess(1)
        }
    }
}
