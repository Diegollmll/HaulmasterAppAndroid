package app.forku.presentation.common.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.Composable


@Composable
fun formatDateTime(dateTimeString: String): String {
    return try {
        val instant = Instant.parse(dateTimeString)
        val localDateTime = LocalDateTime.ofInstant(
            instant,
            ZoneId.systemDefault()
        )
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")
        localDateTime.format(formatter)
    } catch (e: Exception) {
        dateTimeString // Return original string if parsing fails
    }
} 