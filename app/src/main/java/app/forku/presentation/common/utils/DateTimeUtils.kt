package app.forku.presentation.common.utils

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import androidx.compose.runtime.Composable

@Composable
fun formatDateTime(dateTimeString: String): String {
    return try {
        val dateTime = parseToLocalDateTime(dateTimeString)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")
        dateTime.format(formatter)
    } catch (e: Exception) {
        android.util.Log.e("DateTimeUtils", "Error formatting datetime: $dateTimeString", e)
        dateTimeString // Return original string if parsing fails
    }
}

fun getRelativeTimeSpanString(dateTimeStr: String): String {
    return try {
        // First clean the string by removing the timezone in brackets
        val cleanDateStr = dateTimeStr.substringBefore('[')
        val dateTime = OffsetDateTime.parse(cleanDateStr)
        val localDateTime = dateTime.toLocalDateTime()
        
        val now = LocalDateTime.now()
        val minutes = ChronoUnit.MINUTES.between(localDateTime, now)
        val hours = ChronoUnit.HOURS.between(localDateTime, now)
        val days = ChronoUnit.DAYS.between(localDateTime.toLocalDate(), now.toLocalDate())
        
        return when {
            minutes < 1 -> "Just now"
            minutes == 1L -> "1 minute ago"
            minutes < 60 -> "$minutes minutes ago"
            hours == 1L -> "1 hour ago"
            hours < 24 -> "$hours hours ago"
            days == 0L -> "Today at ${localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            days == 1L -> "Yesterday at ${localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            days == -1L -> "Tomorrow at ${localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            days < -1L -> "In ${(-days).toInt()} days"
            days < 7L -> "${days.toInt()} days ago"
            days < 30L -> "${(days / 7).toInt()} weeks ago"
            days < 365L -> "${(days / 30).toInt()} months ago"
            else -> localDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        }
    } catch (e: Exception) {
        android.util.Log.e("DateTimeUtils", "Error parsing datetime: $dateTimeStr", e)
        try {
            // Fallback to simple format if parsing fails
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")
            parseToLocalDateTime(dateTimeStr).format(formatter)
        } catch (e: Exception) {
            dateTimeStr // Return original string if all parsing fails
        }
    }
}

fun parseDateTime(dateTimeStr: String): OffsetDateTime {
    return try {
        // Try parsing as OffsetDateTime directly
        OffsetDateTime.parse(dateTimeStr)
    } catch (e: DateTimeParseException) {
        try {
            // Try parsing as ZonedDateTime and convert to OffsetDateTime
            val zdt = ZonedDateTime.parse(dateTimeStr)
            zdt.toOffsetDateTime()
        } catch (e: DateTimeParseException) {
            try {
                // Try parsing ISO format
                val instant = Instant.parse(dateTimeStr)
                instant.atOffset(ZoneOffset.UTC)
            } catch (e: DateTimeParseException) {
                // If all else fails, try parsing without timezone and use system default
                val cleanDateStr = dateTimeStr.substringBefore('[')
                try {
                    // Try parsing as OffsetDateTime without zone ID
                    OffsetDateTime.parse(cleanDateStr)
                } catch (e: DateTimeParseException) {
                    // Finally, try as plain LocalDateTime
                    LocalDateTime.parse(cleanDateStr).atOffset(ZoneOffset.UTC)
                }
            }
        }
    }
}

fun parseToLocalDateTime(dateTimeStr: String): LocalDateTime {
    return try {
        // Try parsing as OffsetDateTime first (handles ISO format with offset)
        OffsetDateTime.parse(dateTimeStr).toLocalDateTime()
    } catch (e: DateTimeParseException) {
        try {
            // Try parsing as ZonedDateTime (handles timezone in brackets)
            ZonedDateTime.parse(dateTimeStr).toLocalDateTime()
        } catch (e: DateTimeParseException) {
            try {
                // Try parsing as Instant
                Instant.parse(dateTimeStr)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            } catch (e: DateTimeParseException) {
                // Last resort: try parsing the date part before any timezone info
                val cleanDateStr = dateTimeStr.substringBefore('[')
                try {
                    // Try parsing as OffsetDateTime without zone ID
                    OffsetDateTime.parse(cleanDateStr).toLocalDateTime()
                } catch (e: DateTimeParseException) {
                    // Finally, try as plain LocalDateTime
                    LocalDateTime.parse(cleanDateStr)
                }
            }
        }
    }
}

fun getRelativeTimeSpanFromDateTime(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val days = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())
    
    return when {
        days == 0L -> "Today at ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        days == 1L -> "Yesterday at ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        days == -1L -> "Tomorrow at ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        days < -1L -> "In ${(-days).toInt()} days"
        days < 7L -> "${days.toInt()} days ago"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    }
} 