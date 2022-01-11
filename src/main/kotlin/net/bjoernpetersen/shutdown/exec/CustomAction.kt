package net.bjoernpetersen.shutdown.exec

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.core.Promise
import mu.KotlinLogging
import net.bjoernpetersen.shutdown.encodeBase64
import org.stringtemplate.v4.ST
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

sealed class CustomAction {
    protected val logger = KotlinLogging.logger {}

    abstract fun perform(future: Promise<ActionResult>, env: Map<String, Any>)

    companion object {
        val MAPPER = ObjectMapper(YAMLFactory()).apply {
            registerModule(KotlinModule())
            registerModule(CustomActionModule())
        }
    }
}

data class CmdAction(
    val command: List<String>,
    val workingDir: String? = null,
    val detached: Boolean = false,
    val code: Int? = null,
    val ignoreExitCode: Boolean = false
) : CustomAction() {

    override fun perform(future: Promise<ActionResult>, env: Map<String, Any>) {
        val renderedCmd = command.map { it.renderTemplate(env) }
        val process = try {
            ProcessBuilder(renderedCmd)
                .directory(workingDir?.let { File(it) })
                .start()
        } catch (e: IOException) {
            return future.fail(e)
        }
        thread(name = "CmdAction ($renderedCmd)", isDaemon = true, start = true) {
            val output = StringBuilder()
            while (process.isAlive) {
                process.inputStream.bufferedReader().forEachLine {
                    output.appendln(it)
                }
            }
            val resultCode = if (ignoreExitCode || process.exitValue() == 0) {
                logger.debug { "Success." }
                code ?: 200
            } else {
                logger.debug { "Failure with exit code: ${process.exitValue()}" }
                500
            }

            if (!detached) future.complete(ActionResult(output.toString(), resultCode))
        }
        if (detached) future.complete(ActionResult(null, code ?: 202))
    }
}

data class ShortCmdAction(
    val cmd: String,
    val workingDir: String = ".",
    val detached: Boolean = false,
    val code: Int? = null,
    val ignoreExitCode: Boolean = false
) : CustomAction() {
    private val delegate = CmdAction(
        cmd.split(' '),
        workingDir,
        detached,
        code,
        ignoreExitCode
    )

    override fun perform(future: Promise<ActionResult>, env: Map<String, Any>) {
        delegate.perform(future, env)
    }
}

data class PwshAction(
    val pwsh: String,
    val workingDir: String? = null,
    val detached: Boolean = false,
    val code: Int? = null,
    val ignoreExitCode: Boolean = false
) : CustomAction() {

    override fun perform(future: Promise<ActionResult>, env: Map<String, Any>) {
        val renderedCmd = pwsh.renderTemplate(env)
        val encodedCmd = renderedCmd.encodeBase64(Charsets.UTF_16LE)
        val process = try {
            ProcessBuilder("pwsh", "-EncodedCommand", encodedCmd)
                .directory(workingDir?.let { File(it) })
                .start()
        } catch (e: IOException) {
            return future.fail(e)
        }
        thread(name = "PwshAction ($renderedCmd)", isDaemon = true, start = true) {
            val output = StringBuilder()
            while (process.isAlive) {
                process.inputStream.bufferedReader().forEachLine {
                    output.appendln(it)
                }
            }
            val resultCode = if (ignoreExitCode || process.exitValue() == 0) {
                logger.debug { "Success." }
                code ?: 200
            } else {
                logger.debug { "Failure with exit code: ${process.exitValue()}" }
                500
            }

            if (!detached) future.complete(ActionResult(output.toString(), resultCode))
        }
        if (detached) future.complete(ActionResult(null, code ?: 202))
    }
}

data class EchoAction(val echo: String, val code: Int = 200) : CustomAction() {
    override fun perform(future: Promise<ActionResult>, env: Map<String, Any>) {
        val result = echo.renderTemplate(env)
        future.complete(ActionResult(result, code))
    }
}

data class NoContentAction(val content: Boolean) : CustomAction() {
    init {
        if (content) {
            throw IOException("NoContent action must be 'content: false'")
        }
    }

    override fun perform(future: Promise<ActionResult>, env: Map<String, Any>) {
        future.complete(ActionResult(null, 204))
    }
}

private fun String.renderTemplate(env: Map<String, Any>): String {
    return ST(this)
        .apply { env.entries.forEach { add(it.key, it.value) } }
        .render()
}

data class ActionResult(val message: String?, val code: Int) {
    fun succeeded() = code in 200..299
}

private class CustomActionModule : SimpleModule("CustomActionDeserializer") {
    init {
        addDeserializer(CustomAction::class.java, CustomActionDeserializer())
    }
}

private class CustomActionDeserializer : StdDeserializer<CustomAction>(CustomAction::class.java) {
    private val implementations: Map<String, KClass<out CustomAction>> = getImplementations()
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): CustomAction {
        val mapper = jp.codec as ObjectMapper
        val root = mapper.readTree(jp) as ObjectNode
        root.fields().forEach { element ->
            val name = element.key
            val actionClass = implementations[name]
            if (actionClass != null) {
                return mapper.convertValue(root, actionClass.java)
            }
        }
        throw IOException(
            "Does not match any known types. Expected one of: ${implementations.keys}"
        )
    }

    private companion object {
        private fun getImplementations() = CustomAction::class.sealedSubclasses
            .associateBy { it.primaryConstructor?.parameters?.first()?.name!! }
    }
}
