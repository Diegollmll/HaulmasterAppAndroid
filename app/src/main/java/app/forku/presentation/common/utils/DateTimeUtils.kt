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
        // Parse as OffsetDateTime (ISO8601), fallback to Instant
        val instant = try {
            OffsetDateTime.parse(dateTimeStr).toInstant()
        } catch (e: DateTimeParseException) {
            Instant.parse(dateTimeStr)
        }
        val now = Instant.now()
        val duration = Duration.between(instant, now)
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()

        when {
            minutes < 1 -> "Just now"
            minutes == 1L -> "Hace 1 minuto"
            minutes < 60 -> "Hace $minutes minutos"
            hours == 1L -> "Hace 1 hora"
            hours < 24 -> "Hace $hours horas"
            days == 1L -> "Hace 1 día"
            days < 7 -> "Hace $days días"
            days < 30 -> "Hace ${days / 7} semanas"
            days < 365 -> "Hace ${days / 30} meses"
            else -> "Hace ${days / 365} años"
        }
    } catch (e: Exception) {
        "N/A"
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

fun formatReadableDate(dateTimeStr: String): String {
    return try {
        val dateTime = parseToLocalDateTime(dateTimeStr)
        val formatter = DateTimeFormatter.ofPattern("EEE dd MMM yyyy")
        dateTime.format(formatter)
    } catch (e: Exception) {
        android.util.Log.e("DateTimeUtils", "Error formatting readable date: $dateTimeStr", e)
        dateTimeStr // Return original string if parsing fails
    }
} 