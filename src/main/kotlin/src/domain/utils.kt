package src.domain

fun org.joda.time.DateTime.toJavaLocalDate(): java.time.LocalDate =
    java.time.LocalDate.of(this.year, this.monthOfYear, this.dayOfMonth)

fun java.time.LocalDate.toJodaDateTime(): org.joda.time.DateTime =
    org.joda.time.LocalDate(this.year, this.monthValue, this.dayOfMonth).toDateTimeAtStartOfDay()

