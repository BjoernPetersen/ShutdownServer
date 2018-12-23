package net.bjoernpetersen.shutdown.api

import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import net.bjoernpetersen.shutdown.Version
import javax.inject.Inject

class VersionManager @Inject constructor() : EndpointManager {
    private val version: String = Version.get()
    override fun registerHandlers(router: Router) {
        router.route(HttpMethod.GET, "/version")
            .produces("text/plain")
            .handler { ctx ->
                ctx.response().end(version)
            }
    }
}
