package src.view

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

interface ConfirmationService {
    fun confirm(message: String): Boolean
}

class TornadoConfirmationService : ConfirmationService {
    override fun confirm(message: String): Boolean =
        Alert(
            Alert.AlertType.WARNING,
            message,
            ButtonType.YES,
            ButtonType.NO
        )
            .apply { showAndWait() }
            .result == ButtonType.YES
}