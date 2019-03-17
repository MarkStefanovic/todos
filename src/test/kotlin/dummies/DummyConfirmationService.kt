package dummies

import src.framework.ConfirmationService

class DummyConfirmationService : ConfirmationService {
    override fun confirm(message: String) = true
}