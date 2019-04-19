package domain

import java.io.PrintWriter
import java.io.StringWriter

fun org.joda.time.DateTime.toJavaLocalDate(): java.time.LocalDate =
    java.time.LocalDate.of(this.year, this.monthOfYear, this.dayOfMonth)

fun java.time.LocalDate.toJodaDateTime(): org.joda.time.DateTime =
    org.joda.time.LocalDate(this.year, this.monthValue, this.dayOfMonth).toDateTimeAtStartOfDay()

fun stackTraceToString(error: Throwable): String {
    val stringWriter = StringWriter()
    val printWriter = PrintWriter(stringWriter)
    error.printStackTrace(printWriter)
    return stringWriter.toString()
}
