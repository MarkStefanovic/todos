package mockServices

import src.framework.AlertService

class MockAlertService : AlertService {
    var lastErrorMessage: String? = null

    override fun alertError(error: Throwable) {
        lastErrorMessage = error.message
    }
}