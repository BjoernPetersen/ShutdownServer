package net.bjoernpetersen.shutdown

import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.bind
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router

class Api(
    private val configProvider: ConfigProvider,
    private val killer: Killer = WinKiller()) : AbstractVerticle() {

    override fun start() {
        val vertx = getVertx()!!

        val serverConfig = configProvider.bind<ServerConfig>("server")

        val server = vertx.createHttpServer(HttpServerOptions()
            .setPort(serverConfig.port))

        val shutdownConfig = configProvider.bind<ShutdownConfig>("shutdown")

        val router = Router.router(vertx)!!
        router.route(HttpMethod.POST, "/shutdown")
            .handler { ctx ->
                val token: String? = ctx.request().getHeader("token")
                when {
                    token.isNullOrEmpty() -> ctx.fail(401)
                    serverConfig.token != token!!.decode() -> ctx.fail(403)
                    else -> {
                        ctx.response().setStatusCode(204).end()
                        killer.shutDown(shutdownConfig.time)
                    }
                }
            }
        server.requestHandler(router::accept).listen()
    }
}

private fun String.decode(): String? = try {
    decodeBase64()
} catch (e: IllegalArgumentException) {
    null
}
