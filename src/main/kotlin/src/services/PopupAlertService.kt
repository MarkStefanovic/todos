package src.services

import javafx.scene.control.Alert
import mu.KLogging
import src.framework.AlertService

class PopupAlertService : AlertService {
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