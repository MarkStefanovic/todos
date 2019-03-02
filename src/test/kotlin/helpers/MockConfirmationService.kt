package helpers

import src.view.ConfirmationService

class MockConfirmationService : ConfirmationService {
    override fun confirm(message: String) = true
}