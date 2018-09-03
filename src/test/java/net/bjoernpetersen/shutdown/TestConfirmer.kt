package net.bjoernpetersen.shutdown

class TestConfirmer(private val result: Confirmer.Result = Confirmer.Result.OK) : Confirmer {
    override fun confirm(seconds: Int, resultHandler: (Confirmer.Result) -> Unit) {
        resultHandler(result)
    }

    override fun abortInfo(exit: Boolean) {
    }
}
