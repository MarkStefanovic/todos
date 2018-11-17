package model

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
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
        Irregular(11, 4, DayOfWeek.THURSDAY) to LocalDate.of(2010, 11, 25)
    )
        .map { (frequency: Frequency, expected: LocalDate) ->
            DynamicTest.dynamicTest("given today is $referenceDate, when the frequency is $frequency, " +
                                        "then the nextDate should be $expected") {
                assertEquals(expected, getNextDates(frequency, referenceDate = referenceDate))
            }
        }

    @ParameterizedTest
    @CsvSource(
        "2018-01-01, 1970-01-01, 7, 5, true",
        "2018-01-01, 2018-01-01, 7, 5, false",
        "2018-01-01, 2017-12-20, 7, 5, true",  // before advanceDays
        "2018-01-01, 2018-01-07, 7, 5, true"  // after expireDays
    )
    fun testDisplay(nextDate: LocalDate, lastCompleted: LocalDate, advanceDays: Int, expireDays: Int,
                    expected: Boolean) {
        val actual = displayToDo(
            nextDate = nextDate,
            lastCompleted = lastCompleted,
            advanceNotice = advanceDays,
            expireDays = expireDays,
            referenceDate = LocalDate.of(2017, 12, 30)
        )
        assertEquals(expected, actual)
    }
}
