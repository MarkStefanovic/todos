package services

import framework.DatabaseService
import mu.KLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import java.sql.SQLException


class SqlDatabaseService(val url: String, driver: String) : DatabaseService {

    companion object: KLogging()

    init {
        Database.connect(url = url, driver = driver)
    }

    override fun <T> execute(command: () -> T): T? {
        with (TransactionManager.currentOrNew(Connection.TRANSACTION_SERIALIZABLE)) {
            return try {
                command().apply {
                    commit()
                }
            } catch (e: SQLException) {
                Companion.logger.error(e) { "SQL Error: $e" }
                null
            }
        }
    }
}