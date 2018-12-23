package net.bjoernpetersen.shutdown.exec

import mu.KotlinLogging
import java.io.IOException

interface Killer {
    /**
     * Schedules a system shutdown.
     *
     * If either this method or [reboot] has been called before and the operation was not
     * aborted in the meantime, this method does nothing.
     * @param time time in seconds
     */
    fun shutDown(time: Int)

    /**
     * Schedules a system reboot.
     *
     * If either this method or [shutDown] has been called before and the operation was not
     * aborted in the meantime, this method does nothing.
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
    private val abortDelegate: () -> Unit) : Killer {

    private val logger = KotlinLogging.logger {}

    private var scheduled = false

    override fun shutDown(time: Int) {
        if (scheduled) return
        scheduled = true
        try {
            shutDownDelegate(time)
        } catch (e: IOException) {
            logger.error(e) {}
        }
    }

    override fun reboot(time: Int) {
        if (scheduled) return
        scheduled = true
        try {
            rebootDelegate(time)
        } catch (e: IOException) {
            logger.error(e) {}
        }
    }

    override fun abort() {
        try {
            abortDelegate()
            scheduled = false
        } catch (e: IOException) {
            logger.error(e) {}
        }
    }
}

class WinKiller : Killer by DelegateKiller(
    { ProcessBuilder("shutdown", "-s", "-t", "$it").start() },
    { ProcessBuilder("shutdown", "-r", "-t", "$it").start() },
    { ProcessBuilder("shutdown", "-a").start() })

class LinuxKiller : Killer by DelegateKiller(
    { ProcessBuilder("shutdown", "$it").start() },
    { ProcessBuilder("shutdown", "-r", "$it").start() },
    { ProcessBuilder("shutdown", "-c").start() })
