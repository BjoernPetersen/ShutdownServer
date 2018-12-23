package net.bjoernpetersen.shutdown.api

import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.bind
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import net.bjoernpetersen.shutdown.Killer
import net.bjoernpetersen.shutdown.ServerConfig
import net.bjoernpetersen.shutdown.ShutdownConfig
import net.bjoernpetersen.shutdown.WinKiller

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

        // Register auth handler for all routes
        router.route().handler(AuthHandler(serverConfig))

        router.route(HttpMethod.POST, "/shutdown").handler { ctx ->
            ctx.response().setStatusCode(204).end()
            killer.shutDown(shutdownConfig.time)
        }
        server.requestHandler(router::accept).listen()
    }
}

