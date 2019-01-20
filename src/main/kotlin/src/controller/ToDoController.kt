package src.controller

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import org.jetbrains.exposed.sql.*
import src.app.Db
import src.app.toJodaDateTime
import src.model.ToDo
import src.model.ToDos
import src.model.toToDo


class ToDoController(
    val db: Db,
    schedulerProvider: BaseSchedulerProvider
) : BaseController<ToDo>(schedulerProvider) {

    override fun add(newItem: ToDo) {
        db.execute {
            ToDos.insert {
                it[description] = newItem.description
                it[frequency] = newItem.frequency
                it[weekday] = newItem.weekday
                it[monthday] = newItem.monthday
                it[month] = newItem.month
                it[year] = newItem.year
                it[weekNumber] = newItem.weekNumber
                it[startDate] = newItem.startDate.toJodaDateTime()
                it[days] = newItem.days
                it[expireDays] = newItem.expireDays
                it[advanceNotice] = newItem.advanceNotice
                it[note] = newItem.note
            } get ToDos.id
        }?.let { id ->
            byId(id)?.let {
                addResponse.onNext(it)
            }
        }
    }

    override fun delete(id: Int) {
        byId(id)?.let { todo ->
            Alert(
                Alert.AlertType.WARNING,
                "Are you sure you want to delete '${todo.description}'?",
                ButtonType.YES,
                ButtonType.NO
            ).apply {
                showAndWait()
                if (result == ButtonType.YES) {
                    db.execute {
                        ToDos.deleteWhere { ToDos.id eq id }
                    }
                    deleteResponse.onNext(id)
                }
            }
        }
    }

    override fun update(item: ToDo) {
        if ((item.frequency == "Once") and item.complete) {
            delete(item.id)
        } else {
            db.execute {
                ToDos.update({ ToDos.id eq item.id }) {
                    it[description] = item.description
                    it[frequency] = item.frequency
                    it[month] = item.month
                    it[weekday] = item.weekday
                    it[monthday] = item.monthday
                    it[year] = item.year
                    it[weekNumber] = item.weekNumber
                    it[dateAdded] = item.dateAdded.toJodaDateTime()
                    it[dateCompleted] = item.dateCompleted.toJodaDateTime()
                    it[startDate] = item.startDate.toJodaDateTime()
                    it[days] = item.days
                    it[expireDays] = item.expireDays
                    it[advanceNotice] = item.advanceNotice
                    it[note] = item.note
                }
            }
            byId(item.id)?.let { updated ->
                updateResponse.onNext(updated)
            }
        }
    }

    override fun refresh(source: SignalSource) {
        db.execute {
            ToDos.selectAll()
                .map { it.toToDo() }
                .sortedWith(compareBy(ToDo::nextDate, ToDo::description))
        }?.let { todos ->
            refreshResponse.onNext(source to todos)
        }
    }

    override fun filter(request: Pair<SignalSource, Query>) {
        val (token, query) = request
        db.execute {
            query
                .map { it.toToDo() }
                .sortedWith(compareBy(ToDo::nextDate, ToDo::description))
        }?.let { todos ->
            filterResponse.onNext(token to todos)
        }
    }

    private fun byId(id: Int): ToDo? =
        db.execute {
            ToDos.select { ToDos.id eq id }.firstOrNull()
        }?.toToDo()
}