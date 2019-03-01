package src.view

import javafx.scene.control.Alert
import mu.KLogging

interface AlertService {
    fun alertError(error: Throwable)
}

class TornadoAlertService : AlertService {
    companion object : KLogging()

    override fun alertError(error: Throwable) {
        error.message?.let { msg ->
            logger.error(msg)
            tornadofx.alert(
                Alert.AlertType.ERROR,
                "Error",
                msg
            ).show()
        }
    }
}