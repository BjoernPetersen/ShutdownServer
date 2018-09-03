package net.bjoernpetersen.shutdown

class TestKiller : Killer {
    var isKilled = false
        private set
    var isAborted = false
        private set

    override fun shutDown(time: Int) {
        isKilled = true
    }

    override fun abort() {
        isAborted = true
    }
}
