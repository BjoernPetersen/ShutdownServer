package net.bjoernpetersen.shutdown

interface Killer {
    /**
     * Schedules a system shutdown.
     *
     * If this method has been called before and the shutdown was not aborted in the meantime,
     * this method does nothing.
     * @param time time in seconds
     */
    fun shutDown(time: Int)

    /**
     * Aborts scheduled system shutdown.
     */
    fun abort()
}

class WinKiller : Killer {
    private var scheduled = false
    private val abortProcessBuilder = ProcessBuilder("shutdown", "-a")
    override fun shutDown(time: Int) {
        if (scheduled) return
        scheduled = true
        ProcessBuilder("shutdown", "-s", "-t", "$time").start()
    }

    override fun abort() {
        abortProcessBuilder.start()
        scheduled = false
    }
}
