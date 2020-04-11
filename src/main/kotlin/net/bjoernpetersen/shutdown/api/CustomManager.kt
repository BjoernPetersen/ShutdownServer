package net.bjoernpetersen.shutdown.api

import io.vertx.core.Promise
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.nio.file.Files
import java.nio.file.Path
import java.util.HashMap
import javax.inject.Inject
import kotlin.streams.asSequence
import mu.KotlinLogging
import net.bjoernpetersen.shutdown.Args
import net.bjoernpetersen.shutdown.exec.ActionResult
import net.bjoernpetersen.shutdown.exec.CustomAction
import net.bjoernpetersen.shutdown.exec.customEndpoint
import org.stringtemplate.v4.misc.MultiMap

class CustomManager @Inject constructor(
    private val args: Args
) : EndpointManager {
    private val logger = KotlinLogging.logger { }

    override fun registerHandlers(router: Router) {
        val customDir = args.root.resolve("custom")
        if (!Files.isDirectory(customDir)) return
        Files.list(customDir).asSequence()
            .filter {
                Files.isRegularFile(it) && (it.extension == "yml" || it.extension == "yaml")
            }
            .forEach { path ->
                logger.info { "Found custom endpoint file: ${path.fileName}" }
                val endpoint = customEndpoint(path.toFile())
                val apiPath = path.fileNameWithoutExtension
                endpoint.get?.let { actions ->
                    router
                        .route(HttpMethod.GET, "/$apiPath")
                        .handler { bodyless(it, actions) }
                }
                endpoint.delete?.let { actions ->
                    router
                        .route(HttpMethod.DELETE, "/$apiPath")
                        .handler { bodyless(it, actions) }
                }
                endpoint.post?.let { actions ->
                    router
                        .route(HttpMethod.POST, "/$apiPath")
                        .handler { bodyful(it, actions) }
                }
                endpoint.put?.let { actions ->
                    router
                        .route(HttpMethod.PUT, "/$apiPath")
                        .handler { bodyful(it, actions) }
                }
            }
    }

    private fun MutableMap<String, Any>.putHeaderValues(request: HttpServerRequest) {
        val targetMap = MultiMap<String, Any>()
        request.headers().forEach { (key, value) ->
            targetMap.map(key, value)
        }
        put("header", targetMap)
    }

    private fun MutableMap<String, Any>.putQueryValues(request: HttpServerRequest) {
        val targetMap = MultiMap<String, Any>()
        request.params().forEach { (key, value) ->
            targetMap.map(key, value)
        }
        put("query", targetMap)
    }

    private fun MutableMap<String, Any>.putBodyValues(ctx: RoutingContext) {
        if (ctx.body.length() > 0)
            put("body", BodyConverter.toMap(ctx.bodyAsJson))
    }

    private fun bodyful(ctx: RoutingContext, actions: List<CustomAction>) {
        logger.info { "Bodyful" }
        val env: MutableMap<String, Any> = HashMap()
        env.putHeaderValues(ctx.request())
        env.putQueryValues(ctx.request())
        env.putBodyValues(ctx)

        execute(ctx, actions, env.toMap(), 0)
    }

    private fun bodyless(ctx: RoutingContext, actions: List<CustomAction>) {
        val env: MutableMap<String, Any> = HashMap()
        env.putHeaderValues(ctx.request())
        env.putQueryValues(ctx.request())
        execute(ctx, actions, env.toMap(), 0)
    }

    private fun execute(
        ctx: RoutingContext,
        actions: List<CustomAction>,
        env: Map<String, Any>,
        index: Int
    ) {
        val action = actions[index]
        ctx.vertx().executeBlocking({ future: Promise<ActionResult> ->
            action.perform(future, env)
        }, {
            if (it.failed()) {
                ctx.fail(it.cause())
            } else {
                val result = it.result()
                if (result.succeeded()) {
                    if (index == actions.size - 1) {
                        ctx.response()
                            .setStatusCode(result.code)
                            .endNullable(result.message)
                    } else {
                        val message = result.message
                        @Suppress("UNCHECKED_CAST")
                        val outputs = (env["output"] as? Map<String, Any>)
                            ?.toMutableMap() ?: HashMap()
                        outputs[index.toString()] = message ?: ""
                        val newEnv = env.plus("output" to outputs)
                        execute(ctx, actions, newEnv, index + 1)
                    }
                } else {
                    ctx.response()
                        .setStatusCode(result.code)
                        .endNullable(result.message)
                }
            }
        })
    }

    private fun HttpServerResponse.endNullable(obj: String?) {
        if (obj == null) end()
        else end(obj)
    }
}

private object BodyConverter {
    fun toMap(jsonObject: JsonObject): Map<String, Any> {
        val result: MutableMap<String, Any> = HashMap(jsonObject.size() * 2)
        jsonObject.forEach { (key, value) ->
            result[key] = convert(value)
        }
        return result
    }

    private fun toList(jsonArray: JsonArray): List<Any> {
        return jsonArray.map { convert(it) }
    }

    private fun convert(json: Any): Any {
        return when (json) {
            is JsonObject -> toMap(json)
            is JsonArray -> toList(json)
            else -> json
        }
    }
}

private val Path.extension: String
    get() = fileName.toString().substringAfterLast('.', "")

private val Path.fileNameWithoutExtension: String
    get() = fileName.toString().substringBeforeLast('.')
