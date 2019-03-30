package services

import framework.ConfirmationService
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

class PopupConfirmationService : ConfirmationService {
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