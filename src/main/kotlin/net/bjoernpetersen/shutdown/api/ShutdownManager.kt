package net.bjoernpetersen.shutdown.api

import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import net.bjoernpetersen.shutdown.Killer
import net.bjoernpetersen.shutdown.ShutdownConfig
import javax.inject.Inject

class ShutdownManager @Inject constructor(
    private val killer: Killer,
    private val shutdownConfig: ShutdownConfig) : EndpointManager {

    override fun registerHandlers(router: Router) {
        router.route(HttpMethod.POST, "/shutdown").handler { ctx ->
            ctx.response().setStatusCode(204).end()
            killer.shutDown(shutdownConfig.time)
        }
        router.route(HttpMethod.DELETE, "/shutdown").handler { ctx ->
            killer.abort()
            ctx.response().setStatusCode(204).end()
        }
    }
}
