package com.mvukosav.scoreagentsvas.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

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

fun hasMatchStarted(startTime: String): Boolean {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val matchDate = dateFormat.parse(startTime)
    val currentDate = Calendar.getInstance().time

    return matchDate?.before(currentDate) ?: false
}

fun formatTimeMillis(millis: Long?): String? {
    if (millis == null) return null
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    val minutesPart = minutes % 60
    val hoursPart = hours % 24
    val daysPart = days

    return if (daysPart == 0L && hoursPart <= 0L) {
        "${hoursPart}h ${minutesPart}m"
    } else {
        null
    }
}

fun parseTimeFormattedString(timeString: String): Pair<Int, Int> {
    val regex = """(\d+)h (\d+)m""".toRegex()
    val matchResult = regex.matchEntire(timeString)

    return matchResult?.let {
        val (hours, minutes) = it.destructured
        Pair(hours.toInt(), minutes.toInt())
    } ?: Pair(0, 0)
}

fun getTime(startTime: String): String {
    return formatTimeMillis(timeUntilMatchStart(startTime)) ?: ""
}

fun timeUntilMatchStart(startTime: String): Long? {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) // Adjusted format
    return try {
        val matchDate = dateFormat.parse(startTime)
        val currentDate = Calendar.getInstance().time

        if (matchDate != null && currentDate.before(matchDate)) {
            matchDate.time - currentDate.time // Time in milliseconds until match start
        } else {
            null // Match has already started or invalid start time
        }
    } catch (e: ParseException) {
        e.printStackTrace()
        null
    }
}

