package src.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class DisplayArea {
    Reminders, ToDos
}

data class ToDo(
    val id: Int,
    val description: String,
    val frequency: String,
    val month: Int,
    val weekday: Int,
    val monthday: Int,
    val year: Int,
    val weekNumber: Int,
    val dateAdded: LocalDate,
    val dateCompleted: LocalDate,
    val expireDays: Int,
    val advanceNotice: Int,
    val startDate: LocalDate,
    val days: Int,
    val note: String,
    val displayArea: DisplayArea
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
                note = "",
                displayArea = DisplayArea.ToDos
            )
        }
    }

    val daysUntil: Long
        get() = LocalDate.now().until(nextDate, ChronoUnit.DAYS)

    val displayAreaText: String
        get() = displayArea.name

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
            "Irregular" -> Irregular(
                monthValue = month,
                weekNumber = weekNumber,
                weekday = DayOfWeek.of(weekday)
            )
            "Easter" -> Easter
            "XDays" -> XDays(startDate = startDate, days = days)
            else -> Never
        }

    val display: Boolean
        get() = displayToDo(
            nextDate = nextDate,
            advanceDays = advanceNotice,
            expireDays = expireDays,
            lastCompleted = dateCompleted,
            referenceDate = LocalDate.now()
        )

    val nextDate: LocalDate
        get() = frequencyType.nextDate(
            advanceDays = advanceNotice,
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

//    override fun equals(other: Any?): Boolean {
//        return when (other) {
//            !is ToDo -> false
//            id == other.id -> true
//            else -> false
//        }
//    }
//
//    override fun hashCode() = id
}

/** Should the item be displayed on today's to-do list?*/
fun displayToDo(
    nextDate: LocalDate,
    advanceDays: Int,
    expireDays: Int,
    lastCompleted: LocalDate = LocalDate.of(1970, 1, 1),
    referenceDate: LocalDate = LocalDate.now()
): Boolean {
    val advanceStart = nextDate.minusDays(advanceDays.toLong())
    val expireEnd = nextDate.plusDays(expireDays.toLong())
    val lastCompletedInCurrentWindow = (lastCompleted >= advanceStart) and (lastCompleted <= expireEnd)
    val referenceDateInCurrentWindow = (referenceDate >= advanceStart) and (referenceDate <= expireEnd)
    return when {
        lastCompletedInCurrentWindow -> false
        referenceDateInCurrentWindow -> true
        referenceDate == nextDate -> true  // TODO don't allow 0 expireDays in input, and remove this
        else -> false
    }
}