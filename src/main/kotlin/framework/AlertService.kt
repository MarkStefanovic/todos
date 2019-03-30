package framework

interface AlertService {
    fun alertError(error: Throwable)
}