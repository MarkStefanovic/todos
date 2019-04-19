package services

import domain.stackTraceToString
import framework.AlertService
import javafx.application.Platform
import javafx.scene.control.Alert
import mu.KLogging

class PopupAlertService : AlertService {
    companion object : KLogging()

    override fun alertError(error: Throwable) {
        logger.error(stackTraceToString(error))
        error.message?.let { msg ->
            Platform.runLater {
                tornadofx.alert(Alert.AlertType.ERROR, "Error", msg).show()
            }
        }
    }
}