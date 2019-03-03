package src.domain

import org.jetbrains.exposed.sql.*
import src.framework.Repository
import src.services.Db

class ToDoRepository(private val db: Db) : Repository<ToDo>() {
    override fun add(newItem: ToDo): ToDo? =
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
                it[displayArea] = newItem.displayArea.name
            } get ToDos.id
        }?.let { id ->
            byId(id)
        }

    override fun delete(id: Int): Int? =
        byId(id)?.let { todo ->
            db.execute {
                ToDos.deleteWhere { ToDos.id eq id }
            }
        }

    override fun update(item: ToDo): ToDo? =
        if ((item.frequency == "Once") and item.complete) {
            delete(item.id)
            null
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
                    it[displayArea] = item.displayArea.name
                }
            }
            byId(item.id)
        }

    override fun all(): List<ToDo>? =
        db.execute {
            ToDos.selectAll()
                .map { it.toToDo() }
                .filterNotNull()
                .sortedWith(compareBy(ToDo::nextDate, ToDo::description))
        }

    override fun filter(query: Query): List<ToDo>? =
        db.execute {
            query
                .map { it.toToDo() }
                .filterNotNull()
                .sortedWith(compareBy(ToDo::nextDate, ToDo::description))
        }

    private fun byId(id: Int): ToDo? =
        db.execute {
            ToDos.select { ToDos.id eq id }.firstOrNull()
        }?.toToDo()
}