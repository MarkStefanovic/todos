package mockServices

import src.framework.ConfirmationService

class MockConfirmationService : ConfirmationService {
    override fun confirm(message: String) = true
}