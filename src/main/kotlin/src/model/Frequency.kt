package src.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

sealed class Frequency {
    abstract fun nextDate(
        advanceDays: Int = 0,
        expireDays: Int = 0,
        referenceDate: LocalDate = LocalDate.now()
    ): LocalDate
}

object Never : Frequency() {
    override fun nextDate(advanceDays: Int, expireDays: Int, referenceDate: LocalDate): LocalDate =
        LocalDate.of(9999, 12, 31)

    override fun toString() = "never"
}

object Daily : Frequency() {
    override fun nextDate(advanceDays: Int, expireDays: Int, referenceDate: LocalDate): LocalDate = referenceDate

    override fun toString() = "daily"
}

data class Once(val year: Int, val month: Int, val monthday: Int) : Frequency() {
    override fun nextDate(advanceDays: Int, expireDays: Int, referenceDate: LocalDate) =
        LocalDate.of(year, month, monthday)

    override fun toString(): String {
        val dateVal = LocalDate.of(year, month, monthday)
        return dateVal.toString()
    }
}

data class Weekly(val weekday: DayOfWeek) : Frequency() {
    override fun nextDate(advanceDays: Int, expireDays: Int, referenceDate: LocalDate): LocalDate =
        referenceDate.with(TemporalAdjusters.nextOrSame(weekday))

    override fun toString() = "every $weekday"
}

data class Monthly(val monthday: Int) : Frequency() {
    override fun nextDate(advanceDays: Int, expireDays: Int, referenceDate: LocalDate): LocalDate =
        referenceDate.nextMonthDay(
            dayOfMonth = monthday,
            expireDays = expireDays
        )

    override fun toString() = "every $monthday day of the month"
}

data class Yearly(val monthValue: Int, val dayOfMonth: Int) : Frequency() {
    override fun nextDate(advanceDays: Int, expireDays: Int, referenceDate: LocalDate): LocalDate =
        referenceDate.nextYearDay(
            monthValue = monthValue,
            dayOfMonth = dayOfMonth,
            expireDays = expireDays
        )

    override fun toString() = "every $dayOfMonth day of month $monthValue"
}

data class XDays(val startDate: LocalDate, val days: Int) : Frequency() {
    override fun nextDate(advanceDays: Int, expireDays: Int, referenceDate: LocalDate): LocalDate {
        val daysFromStart = ChronoUnit.DAYS.between(startDate, referenceDate)
        val modDays = daysFromStart.rem(days)
        val prior = referenceDate.minusDays(modDays)
        val next = referenceDate.plusDays(days - modDays)
        return when {
            referenceDate == startDate -> startDate
            next.minusDays(advanceDays.toLong()) > referenceDate -> prior
            prior.plusDays(expireDays.toLong()) > referenceDate -> next
            else -> prior
        }
    }
}

data class Irregular(val monthValue: Int, val weekNumber: Int, val weekday: DayOfWeek) : Frequency() {
    override fun nextDate(advanceDays: Int, expireDays: Int, referenceDate: LocalDate): LocalDate =
        referenceDate.nextXWeekdayOfMonth(
            month = monthValue,
            week = weekNumber,
            weekday = weekday
        )

    override fun toString() = "every $weekNumber weeks, weekday $weekday, of month $monthValue"
}

object Easter: Frequency() {
    // src: https://en.wikipedia.org/wiki/Computus
    private fun calculateEaster(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = ((19 * a) + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + (2 * e) + (2 * i) - h - k) % 7
        val m = (a + (11 * h) + (22 * l)) / 451
        val month = (h + l - (7 * m) + 114) / 31
        val day = ((h + l - (7 * m) + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }

    override fun nextDate(advanceDays: Int, expireDays: Int, referenceDate: LocalDate): LocalDate {
        val easterCurrentYear = calculateEaster(referenceDate.year)
        val easterNextYear = calculateEaster(referenceDate.year + 1)
        return if (referenceDate <= easterCurrentYear.plusDays(expireDays.toLong()))
            easterCurrentYear
        else
            easterNextYear
    }

    override fun toString() = "every Easter"
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