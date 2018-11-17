package src.app

import javafx.scene.control.Alert

fun alertError(error: Throwable) {
    logger.error(error.message)

    tornadofx.alert(
        Alert.AlertType.ERROR,
        "Error",
        error.message ?: ""
    ).show()
}

fun org.joda.time.DateTime.toJavaLocalDate(): java.time.LocalDate =
    java.time.LocalDate.of(this.year, this.monthOfYear, this.dayOfMonth)

fun java.time.LocalDate.toJodaDateTime(): org.joda.time.DateTime =
    org.joda.time.LocalDate(this.year, this.monthValue, this.dayOfMonth).toDateTimeAtStartOfDay()

