package net.bjoernpetersen.shutdown

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        val (token, time, port) = readConfig()
        this.token = token
        this.port = port

        killer = TestKiller()
        val lock = ReentrantLock()
        val condition = lock.newCondition()!!
        lock.withLock {
            vertx.deployVerticle(Api(token, time, port, killer, TestConfirmer())) {
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

    @Test
    fun invalidToken(vertx: Vertx) {
        val context = VertxTestContext()

        vertx.createHttpClient()
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

    @Test
    fun correctToken(vertx: Vertx) {
        val context = VertxTestContext()

        vertx.createHttpClient()
            .post(port, "localhost", "/shutdown")
            .putHeader("token", token)
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
