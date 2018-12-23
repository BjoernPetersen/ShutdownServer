package net.bjoernpetersen.shutdown.exec

import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.Future
import java.io.File

private data class CustomDescriptor(
    val shutdown: CustomAction?,
    val reboot: CustomAction?,
    val abort: CustomAction?)

private class CustomKiller(
    private val fallback: Killer,
    private val descriptor: CustomDescriptor) : Killer {

    override fun shutDown(time: Int) {
        if (descriptor.shutdown == null) {
            return fallback.shutDown(time)
        }

        val env = timeEnv(time)
        descriptor.shutdown.performSync(env)
    }

    override fun reboot(time: Int) {
        if (descriptor.reboot == null) {
            return fallback.reboot(time)
        }

        val env = timeEnv(time)
        descriptor.reboot.performSync(env)
    }

    override fun abort() {
        if (descriptor.abort == null) {
            return fallback.abort()
        }

        descriptor.abort.performSync(emptyMap())
    }

    private fun timeEnv(time: Int) = mapOf("time" to time.toString())

    private fun CustomAction.performSync(env: Map<String, Any>) {
        val future = Future.future<ActionResult>()
        perform(future, env)
        while (!future.isComplete) {
            Thread.sleep(200)
        }
    }
}

fun customKiller(killerFile: File, fallback: Killer): Killer {
    val customDescriptor = CustomAction.MAPPER.readValue<CustomDescriptor>(killerFile)
    return CustomKiller(fallback, customDescriptor)
}
