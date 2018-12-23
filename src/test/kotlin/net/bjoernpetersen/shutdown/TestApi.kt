package net.bjoernpetersen.shutdown

import com.jdiazcano.cfg4k.providers.bind
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import net.bjoernpetersen.shutdown.api.Api
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@ExtendWith(VertxExtension::class)
class TestApi {

    private var port: Int = -1
    private lateinit var token: String
    private lateinit var killer: TestKiller

    @BeforeEach
    fun initVertx(vertx: Vertx) {
        val config = readConfig("testConfig.yml")
        val serverConfig = config.bind<ServerConfig>("server")
        this.port = serverConfig.port
        this.token = serverConfig.token

        killer = TestKiller()
        val lock = ReentrantLock()
        val condition = lock.newCondition()!!
        lock.withLock {
            vertx.deployVerticle(Api(config, killer)) {
                assumeTrue(it.succeeded())
                lock.withLock {
                    condition.signal()
                }
            }
            condition.await()
        }
    }

    @Test
    fun noToken(vertx: Vertx) {
        val context = VertxTestContext()

        vertx.createHttpClient()
            .post(port, "localhost", "/shutdown") {
                context.verify {
                    assertEquals(401, it.statusCode())
                    assertFalse(killer.isKilled)
                    context.completeNow()
                }
            }
            .end()

        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS))
        if (context.failed()) {
            fail(context.causeOfFailure())
        }
    }

    @Test
    fun emptyToken(vertx: Vertx) {
        val context = VertxTestContext()

        vertx.createHttpClient()
            .post(port, "localhost", "/shutdown")
            .putHeader("token", "")
            .handler {
                context.verify {
                    assertEquals(401, it.statusCode())
                    assertFalse(killer.isKilled)
                    context.completeNow()
                }
            }
            .end()

        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS))
        if (context.failed()) {
            fail(context.causeOfFailure())
        }
    }

    @TestFactory
    fun invalidToken(vertx: Vertx): List<DynamicTest> {
        val client = vertx.createHttpClient()
        return listOf("literallyinvalid", "wrong".encodeBase64())
            .map { token ->
                dynamicTest(token) {
                    val context = VertxTestContext()
                    client
                        .post(port, "localhost", "/shutdown")
                        .putHeader("token", "invalid")
                        .handler {
                            context.verify {
                                assertEquals(403, it.statusCode())
                                assertFalse(killer.isKilled)
                                context.completeNow()
                            }
                        }
                        .end()

                    assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS))
                    if (context.failed()) {
                        fail(context.causeOfFailure())
                    }
                }
            }
    }

    @Test
    fun correctToken(vertx: Vertx) {
        val context = VertxTestContext()

        vertx.createHttpClient()
            .post(port, "localhost", "/shutdown")
            .putHeader("token", token.encodeBase64())
            .handler {
                context.verify {
                    assertEquals(204, it.statusCode())
                    assertTrue(killer.isKilled)
                    context.completeNow()
                }
            }
            .end()

        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS))
        if (context.failed()) {
            fail(context.causeOfFailure())
        }
    }

}
