package net.bjoernpetersen.shutdown.exec

import java.time.Instant

sealed class KillerState(val isScheduled: Boolean)
data class Scheduled(val time: Instant, val isReboot: Boolean) : KillerState(true)
class Unscheduled : KillerState(false)
