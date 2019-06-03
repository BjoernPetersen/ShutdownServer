@file:Suppress("DEPRECATION")

package net.bjoernpetersen.shutdown.api

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpMethod
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import net.bjoernpetersen.shutdown.Instance
import net.bjoernpetersen.shutdown.InstanceExtension
import net.bjoernpetersen.shutdown.ServerConfig
import net.bjoernpetersen.shutdown.encodeBase64
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import java.util.concurrent.TimeUnit

@ExtendWith(InstanceExtension::class, VertxExtension::class)
interface AuthorizedEndpointTest {

    val path: String
    val method: HttpMethod

    @BeforeEach
    fun initVertx(vertx: Vertx, api: Api) {
        val context = VertxTestContext()

        vertx.deployVerticle(api) {
            if (it.succeeded()) {
                context.completeNow()
            } else {
                context.failNow(it.cause())
            }
        }

        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS)) {
            "Received no response within 5 seconds"
        }
        if (context.failed()) {
            fail(context.causeOfFailure())
        }
    }

    @AfterEach
    fun destroyVertx(vertx: Vertx) {
        val context = VertxTestContext()

        vertx.close {
            if (it.succeeded()) {
                context.completeNow()
            } else {
                context.failNow(it.cause())
            }
        }

        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS)) {
            "Received no response within 5 seconds"
        }
        if (context.failed()) {
            fail(context.causeOfFailure())
        }
    }

    @Test
    fun noToken(vertx: Vertx, serverConfig: ServerConfig, instance: Instance) {
        val context = VertxTestContext()

        vertx.createHttpClient()
            .request(serverConfig)
            .handler {
                context.verify {
                    assertEquals(401, it.statusCode())
                    context.completeNow()
                }
            }
            .end()

        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS)) {
            "Received no response within 5 seconds"
        }
        if (context.failed()) {
            fail(context.causeOfFailure())
        }
    }

    @Test
    fun emptyToken(vertx: Vertx, serverConfig: ServerConfig, instance: Instance) {
        val context = VertxTestContext()

        vertx.createHttpClient()
            .request(serverConfig)
            .putHeader("token", "")
            .handler {
                context.verify {
                    assertEquals(401, it.statusCode())
                    context.completeNow()
                }
            }
            .end()

        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS)) {
            "Received no response within 5 seconds"
        }
        if (context.failed()) {
            fail(context.causeOfFailure())
        }
    }

    @TestFactory
    fun invalidToken(
        vertx: Vertx,
        serverConfig: ServerConfig,
        instance: Instance
    ): List<DynamicTest> {
        val client = vertx.createHttpClient()
        return listOf("literallyinvalid", "wrong".encodeBase64())
            .map { token ->
                dynamicTest(token) {
                    val context = VertxTestContext()
                    client
                        .request(serverConfig)
                        .putHeader("token", "invalid")
                        .handler {
                            context.verify {
                                assertEquals(403, it.statusCode())
                                context.completeNow()
                            }
                        }
                        .end()

                    assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS)) {
                        "Received no response within 5 seconds"
                    }
                    if (context.failed()) {
                        fail(context.causeOfFailure())
                    }
                }
            }
    }

    @Test
    fun correctToken(vertx: Vertx, serverConfig: ServerConfig, instance: Instance) {
        val context = VertxTestContext()

        vertx.createHttpClient()
            .request(serverConfig)
            .putHeader("token", serverConfig.token.encodeBase64())
            .handler {
                context.verify {
                    assertTrue(it.statusCode() in 201..299)
                    context.completeNow()
                }
            }
            .end()

        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS)) {
            "Received no response within 5 seconds"
        }
        if (context.failed()) {
            fail(context.causeOfFailure())
        }
    }

    fun HttpClient.request(serverConfig: ServerConfig) =
        request(method, serverConfig.port, "localhost", path)
}
