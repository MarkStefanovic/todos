package dummies

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import src.domain.DisplayArea
import src.domain.ToDo
import src.domain.ToDos
import src.framework.DatabaseService
import java.sql.Connection
import java.sql.SQLException
import java.time.DayOfWeek


val holidays = listOf(
    ToDo.default().copy(
        id = 1,
        description = "Thanksgiving",
        frequency = "Irregular",
        month = 11,
        weekNumber = 4,
        weekday = DayOfWeek.THURSDAY.value,
        advanceNotice = 14,
        expireDays = 3,
        displayArea = DisplayArea.Reminders
    ),
    ToDo.default().copy(
        id = 2,
        description = "Christmas",
        frequency = "Yearly",
        month = 12,
        monthday = 25,
        advanceNotice = 14,
        expireDays = 3,
        displayArea = DisplayArea.Reminders
    ),
    ToDo.default().copy(
        id = 3,
        description = "Fathers Day",
        frequency = "Irregular",
        month = 6,
        weekNumber = 3,
        weekday = DayOfWeek.SUNDAY.value,
        advanceNotice = 14,
        expireDays = 3,
        displayArea = DisplayArea.Reminders
    ),
    ToDo.default().copy(
        id = 4,
        description = "Mothers Day",
        frequency = "Irregular",
        month = 5,
        weekNumber = 2,
        weekday = DayOfWeek.SUNDAY.value,
        advanceNotice = 14,
        expireDays = 3,
        displayArea = DisplayArea.Reminders
    ),
    ToDo.default().copy(
        id = 5,
        description = "Labor Day",
        frequency = "Irregular",
        month = 9,
        weekNumber = 1,
        weekday = DayOfWeek.MONDAY.value,
        advanceNotice = 14,
        expireDays = 3,
        displayArea = DisplayArea.Reminders
    ),
    ToDo.default().copy(
        id = 6,
        description = "New Year's",
        frequency = "Yearly",
        month = 1,
        monthday = 1,
        advanceNotice = 14,
        expireDays = 3,
        displayArea = DisplayArea.Reminders
    ),
    ToDo.default().copy(
        id = 7,
        description = "Easter",
        frequency = "Easter",
        advanceNotice = 14,
        expireDays = 3,
        displayArea = DisplayArea.Reminders
    )
)


class TestDb : DatabaseService {
    init {
        Database.connect(
            "jdbc:sqlite:file:test?mode=memory&cache=shared",
            "org.sqlite.JDBC"
        )
        resetDb()
    }

    override fun <T> execute(command: () -> T): T? {
        with(TransactionManager.currentOrNew(Connection.TRANSACTION_SERIALIZABLE)) {
            return try {
                command().apply {
                    commit()
                    close()
                }
            } catch (e: SQLException) {
                null
            }
        }
    }
}

fun resetDb() {
    with(TransactionManager.currentOrNew(Connection.TRANSACTION_SERIALIZABLE)) {
        SchemaUtils.drop(ToDos)
        commit()
        SchemaUtils.create(ToDos)
        holidays.forEach { todo ->
            ToDos.insert {
                it[description] = todo.description
                it[frequency] = todo.frequency
                it[weekday] = todo.weekday
                it[monthday] = todo.monthday
                it[month] = todo.month
                it[year] = todo.year
                it[weekNumber] = todo.weekNumber
                it[expireDays] = todo.expireDays
                it[advanceNotice] = todo.advanceNotice
                it[displayArea] = todo.displayArea.name
            }
        }
        commit()
    }
}