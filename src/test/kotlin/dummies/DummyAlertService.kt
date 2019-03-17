package dummies

import src.framework.AlertService

class DummyAlertService : AlertService {
    var lastErrorMessage: String? = null

    override fun alertError(error: Throwable) {
        lastErrorMessage = error.message
    }
}