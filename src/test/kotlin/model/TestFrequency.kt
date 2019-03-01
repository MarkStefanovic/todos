package model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import src.model.*
import java.time.DayOfWeek
import java.time.LocalDate


class TestFrequency {
    private val referenceDate = LocalDate.of(2010, 1, 1)

    @TestFactory
    fun testNextDate() = listOf(
        Daily to LocalDate.of(2010, 1, 1),
        Weekly(DayOfWeek.MONDAY) to LocalDate.of(2010, 1, 4),
        Never to LocalDate.of(9999, 12, 31),
        Monthly(31) to LocalDate.of(2010, 1, 31),
        Yearly(3, 31) to LocalDate.of(2010, 3, 31),
        Irregular(11, 4, DayOfWeek.THURSDAY) to LocalDate.of(2010, 11, 25),
        Once(2010, 1, 31) to LocalDate.of(2010, 1, 31),
        Easter to LocalDate.of(2010, 4, 4),
        XDays(LocalDate.of(2009, 12, 1), 30) to LocalDate.of(2009, 12, 31)
    )
        .map { (frequency: Frequency, expected: LocalDate) ->
            DynamicTest.dynamicTest(
                "given today is $referenceDate, when the frequency is $frequency, " +
                    "then the nextDate should be $expected"
            ) {
                assertEquals(expected, frequency.nextDate(referenceDate = referenceDate))
            }
        }

    @ParameterizedTest
    @CsvSource(
        "2018-01-01, 1970-01-01, 7, 5, true",
        "2018-01-01, 2018-01-01, 7, 5, false",
        "2018-01-01, 2017-12-20, 7, 5, true",  // before advanceDays
        "2018-01-01, 2018-01-07, 7, 5, true"  // after expireDays
    )
    fun testDisplay(
        nextDate: LocalDate, lastCompleted: LocalDate, advanceDays: Int, expireDays: Int,
        expected: Boolean
    ) {
        val actual = displayToDo(
            nextDate = nextDate,
            lastCompleted = lastCompleted,
            advanceDays = advanceDays,
            expireDays = expireDays,
            referenceDate = LocalDate.of(2017, 12, 30)
        )
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "3, 2009-12-31, 5, 2010-01-03",
        "3, 2010-01-01, 5, 2010-01-01",
        "3, 2010-01-01, 0, 2010-01-01"
    )
    fun `test XDays nextDate calculation`(xdays: Int, startDate: LocalDate, expireDays: Int, expected: LocalDate) {
        val actual =
            XDays(startDate = startDate, days = xdays)
                .nextDate(expireDays = expireDays, referenceDate = referenceDate)
        assertEquals(expected, actual)
    }

//    @ParameterizedTest
//    @CsvSource(
//        "3, 2018-10-01, 2018-10-01, 1970-01-01, 7, 5, 2018-10-4",
//        "3, 2018-10-01, 2018-10-01, 1970-01-01, 7, 5, 2018-10-4"
//    )
//    fun `test XDays nextDate calculation`(
//        days: Int, startDate: LocalDate, lastCompleted: LocalDate, advanceDays: Int, expireDays: Int,
//        expected: Boolean) {
//        val actual = XDays(startDate = startDate, days = days).nextDate(
//            expireDays = expireDays, referenceDate = referenceDate
//        )
//        assertEquals(expected, actual)
//    }

    @ParameterizedTest
    @CsvSource(
        "2010-01-01, 0, 1, 2010-12-31, true",
        "2010-01-01, 0, 1, 2010-01-01, false",
        "2010-01-01, 0, 0, 2010-01-01, false",
        "2010-01-02, 1, 1, 2010-01-01, true"
    )
    fun `test XDays display logic`(
        nextDate: LocalDate,
        advanceNotice: Int,
        expireDays: Int,
        lastCompleted: LocalDate,
        expected: Boolean
    ) {
        val actual = displayToDo(
            nextDate = nextDate,
            advanceDays = advanceNotice,
            expireDays = expireDays,
            lastCompleted = lastCompleted,
            referenceDate = referenceDate
        )
        assertEquals(expected, actual)
    }
}
