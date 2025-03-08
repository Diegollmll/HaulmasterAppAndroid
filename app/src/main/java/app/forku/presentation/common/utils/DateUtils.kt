package app.forku.presentation.common.utils

import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun getRelativeTimeSpanString(dateTimeStr: String): String {
    return try {
        val instant = Instant.parse(dateTimeStr)
        val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        getRelativeTimeSpanFromDateTime(dateTime)
    } catch (e: Exception) {
        dateTimeStr // Return original string if parsing fails
    }
}

fun getRelativeTimeSpanFromDateTime(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val days = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())
    
    return when {
        days == 0L -> "Today at ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        days == 1L -> "Yesterday at ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        days < 7L -> "${days.toInt()} days ago"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    }
} 