package controller

import helpers.MockAlertService
import helpers.MockConfirmationService
import helpers.TrampolineSchedulerProvider
import io.reactivex.observers.TestObserver
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import src.app.Db
import src.controller.ToDoController
import src.controller.Token
import src.model.*
import java.time.LocalDate


fun <T>Iterable<T>.isUnique() = this.toSet().count() == this.count()


class TestToDoController {

    private val db = Db(url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver").apply {
        execute {
            if (!ToDos.exists()) {
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
            }
        }
    }

    private val controller = ToDoController(
        db = db,
        schedulerProvider = TrampolineSchedulerProvider(),
        confirmationService = MockConfirmationService(),
        alertService = MockAlertService()
    )

    private fun byDescription(description: String) =
        db.execute {
            ToDos.select { ToDos.description eq description }
            .firstOrNull()?.toToDo()
        } ?: ToDo.default()

    @Test
    fun testRefreshRequest() {
        val testObserver = TestObserver<Pair<Token, List<ToDo>>>()

        controller.refreshResponse.subscribe(testObserver)
        controller.refreshRequest.onNext(Token.TODO_LIST_VIEW)
        testObserver.awaitCount(1)
        testObserver.assertValue { (source, todos) ->
            todos.any { todo -> todo.description == "Thanksgiving" }
        }
        testObserver.assertValue { (_, todos) ->
            todos.isUnique()
        }
    }

    @Test
    fun testAddRequest() {
        val testObserver = TestObserver<ToDo>()
        val todo = ToDo.default().copy(
            description = "Test", frequency = "Once", month = 10, monthday = 23, year = 2018,
            displayArea = DisplayArea.Reminders
        )
        controller.addRequest.subscribe(testObserver)
        controller.addRequest.onNext(todo)
        testObserver.awaitCount(1)
        val dbToDo = byDescription("Test")

        Assertions.assertAll(
            Executable { testObserver.assertValue { it.month == 10 } },
            Executable { testObserver.assertValue { it.year == 2018 } },
            Executable { testObserver.assertValue { it.monthday == 23 } },
            Executable { Assertions.assertEquals(LocalDate.of(2018, 10, 23), dbToDo.onceDate) },
            Executable { Assertions.assertEquals(LocalDate.of(2018, 10, 23), dbToDo.nextDate) }
        )
    }
}