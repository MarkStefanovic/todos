package helpers

import org.jetbrains.exposed.sql.Query
import src.controller.BaseController
import src.controller.BaseSchedulerProvider
import src.controller.Token
import src.model.DisplayArea
import src.model.ToDo
import src.view.AlertService


class MockToDoController(
    alertService: AlertService,
    schedulerProvider: BaseSchedulerProvider
) : BaseController<ToDo>(
    alertService = alertService,
    schedulerProvider = schedulerProvider
) {
    var currentQuery: Query? = null

    val todos = mutableListOf(
        ToDo.default().copy(
            id = 1, frequency = "Once", description = "Test1", year = 2010, month = 10, monthday = 1,
            displayArea = DisplayArea.Reminders
        ),
        ToDo.default().copy(
            id = 2, frequency = "Once", description = "Test2", year = 2010, month = 10, monthday = 2,
            displayArea = DisplayArea.Reminders
        ),
        ToDo.default().copy(
            id = 3, frequency = "Once", description = "Test3", year = 2010, month = 10, monthday = 3,
            displayArea = DisplayArea.Reminders
        )
    )

    override fun add(newItem: ToDo) {
        todos.add(newItem)
        addResponse.onNext(newItem)
    }

    override fun delete(id: Int) {
        todos.removeIf { it.id == id }
        deleteResponse.onNext(id)
    }

    override fun update(item: ToDo) {
        todos.removeIf { it.id == item.id }
        todos.add(item)
        updateResponse.onNext(item)
    }

    override fun refresh(token: Token) {
        refreshResponse.onNext(token to todos)
    }

    override fun filter(request: Pair<Token, Query>) {
        val (source, query) = request
        currentQuery = query
        filterResponse.onNext(source to todos)
    }
}