package net.bjoernpetersen.shutdown

import net.bjoernpetersen.shutdown.exec.Killer
import net.bjoernpetersen.shutdown.exec.KillerState
import net.bjoernpetersen.shutdown.exec.Scheduled
import net.bjoernpetersen.shutdown.exec.Unscheduled
import java.time.Instant

class TestKiller : Killer {
    override var state: KillerState = Unscheduled()
        private set

    val isKilled: Boolean
        get() = state.let { it is Scheduled && !it.isReboot }
    val isRebooted: Boolean
        get() = state.let { it is Scheduled && it.isReboot }
    val isAborted: Boolean
        get() = state.let { it is Unscheduled }

    override fun shutDown(time: Int) {
        state = Scheduled(Instant.now().plusSeconds(time.toLong()), false)
    }

    override fun reboot(time: Int) {
        state = Scheduled(Instant.now().plusSeconds(time.toLong()), true)
    }

    override fun abort() {
        state = Unscheduled()
    }
}
