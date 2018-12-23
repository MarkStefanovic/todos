package helpers

import org.jetbrains.exposed.sql.Query
import src.controller.BaseController
import src.model.ToDo


class MockToDoController: BaseController<ToDo>(TrampolineSchedulerProvider()) {
    var currentQuery: Query? = null

    val todos = mutableListOf(
        ToDo.default().copy(id = 1, frequency = "Once", description = "Test1", year = 2010, month = 10, monthday = 1),
        ToDo.default().copy(id = 2, frequency = "Once", description = "Test2", year = 2010, month = 10, monthday = 2),
        ToDo.default().copy(id = 3, frequency = "Once", description = "Test3", year = 2010, month = 10, monthday = 3)
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

    override fun refresh(token: String) {
        refreshResponse.onNext(token to todos)
    }

    override fun filter(request: Pair<String, Query>) {
        val (token, query) = request
        currentQuery = query
        filterResponse.onNext(token to todos)
    }
}