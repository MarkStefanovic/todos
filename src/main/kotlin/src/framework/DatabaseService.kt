package src.framework

interface DatabaseService {
    fun <T> execute(command: () -> T): T?
}




