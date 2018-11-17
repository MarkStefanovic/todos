package src.app

import mu.KLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import java.sql.SQLException


class Db(val url: String, driver: String) {

    companion object: KLogging()

    init {
        Database.connect(url = url, driver = driver)
    }

    fun <T> execute(command: () -> T) : T? {
        with (TransactionManager.currentOrNew(Connection.TRANSACTION_SERIALIZABLE)) {
            return try {
                command().apply {
                    commit()
                    close()
                }
            } catch (e: SQLException) {
                Companion.logger.error(e) { "SQL Error: $e" }
                null
            }
        }
    }
}