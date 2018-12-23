package net.bjoernpetersen.shutdown

import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router

class Api(private val token: String,
    private val shutdownTime: Int,
    private val port: Int,
    private val killer: Killer = WinKiller(),
    private val confirmer: Confirmer = FxConfirmer()) :
    AbstractVerticle() {

    private fun abort(killer: Killer, exit: Boolean = false) {
        killer.abort()
        confirmer.abortInfo(exit)
    }

    override fun start() {
        val vertx = getVertx()!!
        val server = vertx.createHttpServer(HttpServerOptions().setPort(port))
        val router = Router.router(vertx)!!
        router.route(HttpMethod.POST, "/shutdown")
            .handler { ctx ->
                val token: String? = ctx.request().getHeader("token")
                when {
                    token.isNullOrEmpty() -> ctx.fail(401)
                    this.token != token -> ctx.fail(403)
                    else -> {
                        ctx.response().setStatusCode(204).end()
                        killer.shutDown(shutdownTime)
                        confirmer.confirm(shutdownTime) {
                            when (it) {
                                Confirmer.Result.ABORT -> abort(killer)
                                Confirmer.Result.EXIT -> {
                                    abort(killer, true)
                                    vertx.close()
                                }
                                else -> {
                                }
                            }
                        }
                    }
                }
            }
        server.requestHandler(router::accept).listen()
    }

}
