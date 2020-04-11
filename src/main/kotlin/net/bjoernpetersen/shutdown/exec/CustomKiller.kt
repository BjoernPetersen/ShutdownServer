package net.bjoernpetersen.shutdown.exec

import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.Promise
import java.io.File
import java.time.Instant
import kotlin.math.max

private data class CustomDescriptor(
    val shutdown: CustomAction?,
    val reboot: CustomAction?,
    val abort: CustomAction?
)

private class CustomKiller(
    private val fallback: Killer,
    private val descriptor: CustomDescriptor
) : Killer {

    override var state: KillerState = Unscheduled()
        private set

    override fun shutDown(time: Int) {
        if (state.isScheduled) return
        state = Scheduled(Instant.now().plusSeconds(time.toLong()), false)

        if (descriptor.shutdown == null) {
            return fallback.shutDown(time)
        }

        val env = timeEnv(time)
        descriptor.shutdown.performSync(env)
    }

    override fun reboot(time: Int) {
        if (state.isScheduled) return
        state = Scheduled(Instant.now().plusSeconds(time.toLong()), true)

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

        state = Unscheduled()
    }

    private fun timeEnv(time: Int) = mapOf(
        "time" to time.toString(),
        "seconds" to time.toString(),
        "minutes" to (if (time == 0) 0 else max(1, time / 60)).toString()
    )

    private fun CustomAction.performSync(env: Map<String, Any>) {
        val promise = Promise.promise<ActionResult>()
        val future = promise.future()
        perform(promise, env)
        while (!future.isComplete) {
            Thread.sleep(100)
        }
    }
}

fun customKiller(killerFile: File, fallback: Killer): Killer {
    val customDescriptor = CustomAction.MAPPER.readValue<CustomDescriptor>(killerFile)
    return CustomKiller(fallback, customDescriptor)
}
