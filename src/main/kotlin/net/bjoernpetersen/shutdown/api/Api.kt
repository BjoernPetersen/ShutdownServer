package net.bjoernpetersen.shutdown.api

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import net.bjoernpetersen.shutdown.ServerConfig
import javax.inject.Inject

class Api @Inject constructor(
    private val serverConfig: ServerConfig,
    private val versionManager: VersionManager,
    private val shutdownManager: ShutdownManager,
    private val customManager: CustomManager) : AbstractVerticle() {

    override fun start(future: Future<Void>) {
        vertx.executeBlocking({ result: Future<in Any> ->
            val server = vertx.createHttpServer(HttpServerOptions()
                .setPort(serverConfig.port))

            val router = Router.router(vertx)!!

            router.route().handler(BodyHandler.create())

            // Register auth handler for all routes
            router.route().handler(AuthHandler(serverConfig))

            router
                .registerHandlers(versionManager)
                .registerHandlers(shutdownManager)
                .registerHandlers(customManager)

            server.requestHandler(router).listen(result::handle)
        }, {
            if (it.succeeded()) {
                future.complete()
            } else {
                future.fail(it.cause())
            }
        })
    }
}

private fun Router.registerHandlers(endpointManager: EndpointManager): Router = this.apply {
    endpointManager.registerHandlers(this)
}

interface EndpointManager {

    /**
     * Register handlers for one or multiple routes.
     */
    fun registerHandlers(router: Router)
}
