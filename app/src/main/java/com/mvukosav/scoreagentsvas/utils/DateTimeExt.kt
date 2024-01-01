package com.mvukosav.scoreagentsvas.utils

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun String.formatToNewPattern(newPattern: String = "dd:MM:yyyy HH:mm:ss"): String {
    return try {
        val parsedDate = OffsetDateTime.parse(this)
        val formatter = DateTimeFormatter.ofPattern(newPattern)
        parsedDate.format(formatter)
    } catch (e: Exception) {
        e.printStackTrace()
        "Invalid Date"
    }
}
