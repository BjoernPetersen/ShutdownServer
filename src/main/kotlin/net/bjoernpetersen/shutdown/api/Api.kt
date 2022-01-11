package net.bjoernpetersen.shutdown.api

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import mu.KotlinLogging
import net.bjoernpetersen.shutdown.ServerConfig
import javax.inject.Inject

class Api @Inject constructor(
    private val serverConfig: ServerConfig,
    private val authHandler: AuthHandler,
    private val versionManager: VersionManager,
    private val shutdownManager: ShutdownManager,
    private val customManager: CustomManager
) : AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    override fun start(promise: Promise<Void>) {
        vertx.executeBlocking({ result: Promise<in Any> ->
            logger.info { "Binding server on port ${serverConfig.port}" }

            sequenceOf(DatabindCodec.mapper(), DatabindCodec.prettyMapper()).forEach {
                it.registerModule(
                    KotlinModule.Builder()
                        .build()
                )
            }

            val server = vertx.createHttpServer(
                HttpServerOptions()
                    .setPort(serverConfig.port)
            )

            val router = Router.router(vertx)!!

            router.route().handler(BodyHandler.create())

            // Register auth handler for all routes
            router.route().handler(authHandler)

            router
                .registerHandlers(versionManager)
                .registerHandlers(shutdownManager)
                .registerHandlers(customManager)

            router.errorHandler(500) {
                val failure = it.failure()
                if (failure != null) logger.error(failure) { "Unhandled error" }
            }

            server.requestHandler(router).listen(result::handle)
        }, {
            if (it.succeeded()) {
                promise.complete()
            } else {
                promise.fail(it.cause())
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
