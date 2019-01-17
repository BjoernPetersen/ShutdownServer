package net.bjoernpetersen.shutdown.api

import io.vertx.core.http.HttpMethod
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import net.bjoernpetersen.shutdown.ShutdownConfig
import net.bjoernpetersen.shutdown.exec.Killer
import javax.inject.Inject

class ShutdownManager @Inject constructor(
    private val killer: Killer,
    private val shutdownConfig: ShutdownConfig) : EndpointManager {

    private val logger = KotlinLogging.logger { }
    override fun registerHandlers(router: Router) {
        if (!shutdownConfig.enable) {
            logger.info { "Shutdown endpoints are disabled!" }
            return
        }
        router.route(HttpMethod.GET, "/shutdown").handler { ctx ->
            ctx.response().end(Json.encode(killer.state))
        }
        router.route(HttpMethod.POST, "/shutdown").handler { ctx ->
            ctx.response().setStatusCode(204).end()
            val reboot = ctx.queryParam("reboot")?.firstOrNull()?.toBoolean() ?: false
            if (reboot) killer.reboot(ctx.time())
            else killer.shutDown(ctx.time())
        }
        router.route(HttpMethod.DELETE, "/shutdown").handler { ctx ->
            killer.abort()
            ctx.response().setStatusCode(204).end()
        }
    }

    private fun RoutingContext.time(): Int {
        val time = request().params()["time"]
        return time?.toIntOrNull() ?: shutdownConfig.time
    }
}
