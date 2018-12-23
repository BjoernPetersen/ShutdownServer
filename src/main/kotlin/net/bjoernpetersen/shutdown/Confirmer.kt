package net.bjoernpetersen.shutdown

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

interface Confirmer {
    enum class Result {
        OK, ABORT, EXIT
    }

    fun confirm(seconds: Int, resultHandler: (Result) -> Unit)
    fun abortInfo(exit: Boolean)
}

class FxConfirmer : Confirmer {
    override fun confirm(seconds: Int, resultHandler: (Confirmer.Result) -> Unit) {
        Platform.runLater {
            Alert(Alert.AlertType.WARNING,
                "Computer will shutdown in $seconds seconds! OK or cancel?",
                ButtonType.OK, ButtonType.CANCEL, ButtonType.FINISH)
                .showAndWait()
                .ifPresent {
                    resultHandler(when (it) {
                        ButtonType.CANCEL -> Confirmer.Result.ABORT
                        ButtonType.FINISH -> Confirmer.Result.EXIT
                        else -> Confirmer.Result.OK
                    })
                }
        }
    }

    override fun abortInfo(exit: Boolean) {
        Platform.runLater {
            Alert(Alert.AlertType.INFORMATION,
                "Shutdown canceled.")
                .showAndWait()
            if (exit) Platform.exit()
        }
    }
}
