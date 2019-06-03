package net.bjoernpetersen.shutdown.exec

import mu.KotlinLogging
import java.io.IOException
import java.time.Instant
import kotlin.math.max

interface Killer {
    val state: KillerState

    /**
     * Schedules a system shutdown.
     *
     * If either this method or [reboot] has been called before and the operation was not
     * aborted in the meantime, this method will abort the previous operation first, effectively
     * overriding it.
     *
     * @param time time in seconds
     */
    fun shutDown(time: Int)

    /**
     * Schedules a system reboot.
     *
     * If either this method or [shutDown] has been called before and the operation was not
     * aborted in the meantime, this method will abort the previous operation first, effectively
     * overriding it.
     *
     * @param time time in seconds
     */
    fun reboot(time: Int)

    /**
     * Aborts scheduled system shutdown or reboot.
     */
    fun abort()
}

private class DelegateKiller(
    private val shutDownDelegate: (Int) -> Unit,
    private val rebootDelegate: (Int) -> Unit,
    private val abortDelegate: () -> Unit
) : Killer {

    private val logger = KotlinLogging.logger {}

    override var state: KillerState = Unscheduled()
        private set
    private val isScheduled: Boolean
        get() = state.isScheduled

    override fun shutDown(time: Int) {
        if (isScheduled) abort()
        state = Scheduled(Instant.now().plusSeconds(time.toLong()), false)
        try {
            shutDownDelegate(time)
        } catch (e: IOException) {
            logger.error(e) {}
        }
    }

    override fun reboot(time: Int) {
        if (isScheduled) abort()
        state = Scheduled(Instant.now().plusSeconds(time.toLong()), true)
        try {
            rebootDelegate(time)
        } catch (e: IOException) {
            logger.error(e) {}
        }
    }

    override fun abort() {
        try {
            abortDelegate()
        } catch (e: IOException) {
            logger.error(e) {}
        }
        state = Unscheduled()
    }
}

class WinKiller : Killer by DelegateKiller(
    { ProcessBuilder("shutdown", "-s", "-t", "$it").start() },
    { ProcessBuilder("shutdown", "-r", "-t", "$it").start() },
    { ProcessBuilder("shutdown", "-a").start() })

class LinuxKiller : Killer by DelegateKiller(
    { ProcessBuilder("shutdown", "${if (it == 0) 0 else max(1, it / 60)}").start() },
    { ProcessBuilder("shutdown", "-r", "$it").start() },
    { ProcessBuilder("shutdown", "-c").start() })
