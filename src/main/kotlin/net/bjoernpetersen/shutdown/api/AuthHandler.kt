package net.bjoernpetersen.shutdown.api

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import net.bjoernpetersen.shutdown.ServerConfig
import net.bjoernpetersen.shutdown.decodeBase64

class AuthHandler(serverConfig: ServerConfig) : Handler<RoutingContext> {
    private val token: String = serverConfig.token

    override fun handle(ctx: RoutingContext) {
        val token: String? = ctx.request().getHeader("token")
        when {
            token.isNullOrEmpty() -> ctx.fail(401)
            this.token != token.decode() -> ctx.fail(403)
            else -> ctx.next()
        }
    }

    private fun String.decode(): String? = try {
        decodeBase64()
    } catch (e: IllegalArgumentException) {
        null
    }
}
