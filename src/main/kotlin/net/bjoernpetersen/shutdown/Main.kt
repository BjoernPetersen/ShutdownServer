@file:JvmName("Main")

package net.bjoernpetersen.shutdown

import io.github.cdimascio.dotenv.dotenv
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun readConfig(): Triple<String, Int, Int> {
    val env = dotenv {
        directory = "config"
    }
    val token = env["TOKEN"]
    if (token == null || token.isBlank()) {
        System.err.println("TOKEN is missing from .env file!")
        exitProcess(1)
    }

    val time = env["TIME"]?.toIntOrNull()
    if (time == null || time < 0) {
        System.err.println("TIME is missing from .env file or negative!")
        exitProcess(2)
    }

    val port = env["PORT"]?.toIntOrNull()
    if (port == null || port < 0) {
        System.err.println("PORT is missing from .env file or negative!")
        exitProcess(3)
    }

    return Triple(token, time, port)
}

fun main(args: Array<String>) {
    val (token, time, port) = readConfig()

    LoggerFactory.getLogger("net.bjoernpetersen.shutdown.Main")
        .info("Starting server on port $port")

    val vertx = Vertx.vertx()
    vertx.deployVerticle(Api(token, time, port), DeploymentOptions())
}
