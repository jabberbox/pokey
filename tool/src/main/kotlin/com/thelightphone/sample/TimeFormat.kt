package com.thelightphone.sample

import java.time.format.DateTimeFormatter

enum class TimeFormat(val label: String) {
    HOUR_12("12hr"),
    HOUR_24("24hr"),
}

internal fun TimeFormat.timeFormatter(): DateTimeFormatter =
    DateTimeFormatter.ofPattern(if (this == TimeFormat.HOUR_24) "HH:mm" else "h:mm a")

internal fun TimeFormat.dateTimeFormatter(): DateTimeFormatter =
    DateTimeFormatter.ofPattern(if (this == TimeFormat.HOUR_24) "MMM d, yyyy HH:mm" else "MMM d, yyyy h:mm a")
