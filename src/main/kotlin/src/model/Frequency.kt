package src.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

sealed class Frequency

object Never : Frequency() {
    override fun toString() = "never"
}

object Daily : Frequency() {
    override fun toString() = "daily"
}

data class Once(val year: Int, val month: Int, val monthday: Int) : Frequency() {
    override fun toString(): String {
        val dateVal = LocalDate.of(year, month, monthday)
        return dateVal.toString()
    }
}

data class Weekly(val weekday: DayOfWeek) : Frequency() {
    override fun toString() = "every $weekday"
}

data class Monthly(val monthday: Int) : Frequency() {
    override fun toString() = "every $monthday day of the month"
}

data class Yearly(val monthValue: Int, val dayOfMonth: Int) : Frequency() {
    override fun toString() = "every $dayOfMonth day of month $monthValue"
}

data class Irregular(val monthValue: Int, val weekNumber: Int, val weekday: DayOfWeek) : Frequency() {
    override fun toString() = "every $weekNumber weeks, weekday $weekday, of month $monthValue"
}

fun LocalDate.nextMonthDay(dayOfMonth: Int, expireDays: Int = 20): LocalDate {
    val thisMonth = LocalDate.of(this.year, this.monthValue, dayOfMonth)
    val nextMonth = thisMonth.plusMonths(1)
    return if (this <= thisMonth.plusDays(expireDays.toLong())) thisMonth else nextMonth
}

fun LocalDate.nextYearDay(monthValue: Int, dayOfMonth: Int, expireDays: Int = 30): LocalDate {
    val thisYear = LocalDate.of(this.year, monthValue, dayOfMonth)
    val nextYear = LocalDate.of(this.year + 1, monthValue, dayOfMonth)
    return if (this <= thisYear.plusDays(expireDays.toLong())) thisYear else nextYear
}

fun LocalDate.nextXWeekdayOfMonth(month: Int, weekday: DayOfWeek, week: Int): LocalDate {
    val xWeek = { yr: Int ->
        LocalDate.of(yr, month, 1)
            .with(TemporalAdjusters.nextOrSame(weekday))
            .plusWeeks(week.toLong() - 1)
    }
    val thisYear = xWeek(this.year)
    return if (thisYear > this)
        thisYear
    else
        xWeek(this.year + 1)
}

fun getNextDates(
    frequency: Frequency,
    expireDays: Int = 0,
    referenceDate: LocalDate = LocalDate.now()
): LocalDate =
    when (frequency) {
        is Once -> LocalDate.of(frequency.year, frequency.month, frequency.monthday)
        is Daily -> referenceDate
        is Weekly -> referenceDate.with(TemporalAdjusters.nextOrSame(frequency.weekday))
        is Monthly -> referenceDate.nextMonthDay(
            dayOfMonth = frequency.monthday,
            expireDays = expireDays
        )
        is Yearly -> referenceDate.nextYearDay(
            monthValue = frequency.monthValue,
            dayOfMonth = frequency.dayOfMonth,
            expireDays = expireDays
        )
        is Irregular -> referenceDate.nextXWeekdayOfMonth(
            month = frequency.monthValue, week = frequency.weekNumber,
            weekday = frequency.weekday
        )
        is Never -> LocalDate.of(9999, 12, 31)
    }


fun getWeekdayByName(weekdayName: String) =
    when (weekdayName) {
        "Monday" -> 1
        "Tuesday" -> 2
        "Wednesday" -> 3
        "Thursday" -> 4
        "Friday" -> 5
        "Saturday" -> 6
        "Sunday" -> 7
        else -> throw Exception("$weekdayName is not a valid weekday")
    }