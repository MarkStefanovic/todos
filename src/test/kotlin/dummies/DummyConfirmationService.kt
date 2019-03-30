package dummies

import framework.ConfirmationService

class DummyConfirmationService : ConfirmationService {
    override fun confirm(message: String) = true
}