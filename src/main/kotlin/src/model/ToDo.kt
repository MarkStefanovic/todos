package src.model

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import src.app.logger
import src.app.toJavaLocalDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit


data class ToDo(
    val id : Int,
    val description : String,
    val frequency : String,
    val month : Int,
    val weekday : Int,
    val monthday : Int,
    val year : Int,
    val weekNumber : Int,
    val dateAdded : LocalDate,
    val dateCompleted : LocalDate,
    val expireDays : Int,
    val advanceNotice : Int,
    val startDate : LocalDate,
    val days : Int,
    val note : String
) {
    companion object {
        val NONE_DATE = LocalDate.of(1970, 1, 1)

        fun default(): ToDo {
            val today = LocalDate.now()
            return ToDo(
                id = -1,
                description = "",
                frequency = "Once",
                year = today.year,
                month = today.monthValue,
                monthday = today.dayOfMonth,
                weekday = 1,
                weekNumber = 1,
                dateAdded = today,
                dateCompleted = NONE_DATE,
                expireDays = 90,
                advanceNotice = 0,
                startDate = today,
                days = 1,
                note = ""
            )
        }
    }

    val daysUntil: Long
        get() = LocalDate.now().until(nextDate, ChronoUnit.DAYS)

    val weekdayName: String
        get() = when (weekday) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "NA"
        }

    private val frequencyType: Frequency
        get() = when (frequency) {
            "Once" -> Once(year = year, month = month, monthday = monthday)
            "Daily" -> Daily
            "Weekly" -> Weekly(weekday = DayOfWeek.of(weekday))
            "Monthly" -> Monthly(monthday = monthday)
            "Yearly" -> Yearly(monthValue = month, dayOfMonth = monthday)
            "Irregular" -> Irregular(monthValue = month, weekNumber = weekNumber, weekday = DayOfWeek.of(weekday))
            "Easter" -> Easter
            "XDays" -> XDays(startDate = startDate, days = days)
            else -> Never
        }

    val display: Boolean
        get() = displayToDo(
            nextDate = nextDate,
            advanceNotice = advanceNotice,
            expireDays = expireDays,
            lastCompleted = dateCompleted,
            referenceDate = LocalDate.now()
        )

    val nextDate: LocalDate
        get() = frequencyType.nextDate(
            expireDays = expireDays,
            referenceDate = (
                if (dateCompleted >= LocalDate.now()) dateCompleted
                else LocalDate.now()
            )
        )

    val onceDate: LocalDate
        get() = LocalDate.of(year, month, monthday)

    val complete: Boolean
        get() = !display and (dateCompleted != NONE_DATE)

    val item: ToDo
        get() = this
}

object ToDos: Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val description = varchar("description", 100)
    val frequency = varchar("frequency", 20).default("Once")
    val month = integer("month").default(1)
    val weekday = integer("weekday").default(1)
    val monthday = integer("monthday").default(1)
    val year = integer("year").default(1970)
    val weekNumber = integer("week_number").default(1)
    val dateAdded = date("date_added").default(DateTime.now())
    val dateCompleted = date("date_completed").default(DateTime.parse("1970-01-01"))
    val expireDays = integer("expire_days").default(5)
    val advanceNotice = integer("advance_notice").default(0)
    val startDate = date("start_date").default(DateTime.parse("1970-01-01"))
    val days = integer("days").default(0)
    val note = text("note").default("")
}

fun ResultRow.toToDo() =
    try {
        ToDo(
            id = this[ToDos.id],
            description = this[ToDos.description],
            frequency = this[ToDos.frequency],
            month = this[ToDos.month],
            weekday = this[ToDos.weekday],
            monthday = this[ToDos.monthday],
            year = this[ToDos.year],
            weekNumber = this[ToDos.weekNumber],
            dateAdded = this[ToDos.dateAdded].toJavaLocalDate(),
            dateCompleted = this[ToDos.dateCompleted].toJavaLocalDate(),
            expireDays = this[ToDos.expireDays],
            advanceNotice = this[ToDos.advanceNotice],
            startDate = this[ToDos.startDate].toJavaLocalDate(),
            days = this[ToDos.days],
            note = this[ToDos.note]
        )
    } catch (e: Exception) {
        logger.error { "There was an error converting the row $this to a ToDo: $e" }
        ToDo.default()
    }


/** initial holidays to add the the db */
val holidays: Set<ToDo> = setOf(
    ToDo.default().copy(description = "Thanksgiving", frequency = "Irregular", month = 11, weekNumber = 4,
                          weekday = DayOfWeek.THURSDAY.value, advanceNotice = 14, expireDays = 3),
    ToDo.default().copy(description = "Christmas", frequency = "Yearly", month = 12, monthday = 25,
                          advanceNotice = 14, expireDays = 3),
    ToDo.default().copy(description = "Fathers Day", frequency = "Irregular", month = 6, weekNumber = 3,
                          weekday = DayOfWeek.SUNDAY.value, advanceNotice = 14, expireDays = 3),
    ToDo.default().copy(description = "Mothers Day", frequency = "Irregular", month = 5, weekNumber = 2,
                          weekday = DayOfWeek.SUNDAY.value, advanceNotice = 14, expireDays = 3),
    ToDo.default().copy(description = "Labor Day", frequency = "Irregular", month = 9, weekNumber = 1,
                        weekday = DayOfWeek.MONDAY.value, advanceNotice = 14, expireDays = 3),
    ToDo.default().copy(description = "New Year's", frequency = "Yearly", month = 1, monthday = 1,
                        advanceNotice = 14, expireDays = 3),
    ToDo.default().copy(description = "Easter", frequency = "Easter", advanceNotice = 14, expireDays = 3)
)
/** initial birthdays to add the the db */
val birthdays: Set<ToDo> = setOf(
    ToDo.default().copy(description = "Fathers Day", frequency = "Irregular", month = 6, weekNumber = 3,
                        weekday = DayOfWeek.SUNDAY.value, advanceNotice = 14, expireDays = 5),
    ToDo.default().copy(description = "Mothers Day", frequency = "Irregular", month = 5, weekNumber = 2,
                        weekday = DayOfWeek.SUNDAY.value, advanceNotice = 14, expireDays = 5),
    ToDo.default().copy(description = "Jessie's Birthday", frequency = "Yearly", month = 8, monthday = 24,
                        advanceNotice = 30, expireDays = 7),
    ToDo.default().copy(description = "Sarah's Birthday", frequency = "Yearly", month = 9, monthday = 2,
                        advanceNotice = 30, expireDays = 7),
    ToDo.default().copy(description = "Emma's Birthday", frequency = "Yearly", month = 10, monthday = 10,
                        advanceNotice = 30, expireDays = 7),
    ToDo.default().copy(description = "Dad's Birthday", frequency = "Yearly", month = 11, monthday = 20,
                        advanceNotice = 30, expireDays = 7),
    ToDo.default().copy(description = "Kellen's Birthday", frequency = "Yearly", month = 3, monthday = 30,
                        advanceNotice = 30, expireDays = 7),
    ToDo.default().copy(description = "Mandie's Birthday", frequency = "Yearly", month = 5, monthday = 13,
                        advanceNotice = 30, expireDays = 7),
    ToDo.default().copy(description = "Summer's Birthday", frequency = "Yearly", month = 2, monthday = 24,
                        advanceNotice = 30, expireDays = 7)
)

/** Should the item be displayed on today's to-do list?*/
fun displayToDo(
    nextDate: LocalDate,
    advanceNotice: Int,
    expireDays: Int,
    lastCompleted: LocalDate = LocalDate.of(1970, 1, 1),
    referenceDate: LocalDate = LocalDate.now()
): Boolean =
    when {
        (lastCompleted >= nextDate.minusDays(advanceNotice.toLong())) and
            (lastCompleted <= nextDate.plusDays(expireDays.toLong())) -> false
        (referenceDate >= nextDate.minusDays(advanceNotice.toLong())) and
            (referenceDate <= nextDate.plusDays(expireDays.toLong())) -> true
        else -> false
    }
