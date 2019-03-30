package framework

interface ConfirmationService {
    fun confirm(message: String): Boolean
}
