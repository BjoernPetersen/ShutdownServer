package net.bjoernpetersen.shutdown.api

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.junit5.VertxTestContext
import net.bjoernpetersen.shutdown.Instance
import net.bjoernpetersen.shutdown.ServerConfig
import net.bjoernpetersen.shutdown.encodeBase64
import net.bjoernpetersen.shutdown.exec.TestKiller
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.util.concurrent.TimeUnit

class ShutdownAbortTest : AuthorizedEndpointTest {
    override val path = "/shutdown"
    override val method: HttpMethod = HttpMethod.DELETE

    @Suppress("DEPRECATION")
    @Test
    fun killerAborted(vertx: Vertx, serverConfig: ServerConfig, instance: Instance) {
        val context = VertxTestContext()

        vertx.createHttpClient()
            .request(serverConfig)
            .putHeader("token", serverConfig.token.encodeBase64())
            .handler {
                context.verify {
                    assertTrue(it.statusCode() in 201..299)
                    val killer = instance.killer as TestKiller
                    assertTrue(killer.isAborted)
                    context.completeNow()
                }
            }
            .end()

        Assertions.assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS))
        if (context.failed()) {
            fail(context.causeOfFailure())
        }
    }
}
