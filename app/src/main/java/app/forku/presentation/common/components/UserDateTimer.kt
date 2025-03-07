package app.forku.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
fun UserDateTimer(
    modifier: Modifier = Modifier,
    initialDateTime: LocalDateTime = LocalDateTime.now(),
    sessionStartTime: LocalDateTime? = null
) {
    var dateTime by remember { mutableStateOf(initialDateTime) }
    var elapsedTime by remember { mutableStateOf("00:00:00") }
    
    LaunchedEffect(sessionStartTime) {
        // Only start the timer if there's a session start time
        if (sessionStartTime != null) {
            while(true) {
                delay(1000)
                dateTime = LocalDateTime.now()
                
                // Calculate duration from session start to current time
                val duration = java.time.Duration.between(sessionStartTime, dateTime)
                val hours = duration.toHours().absoluteValue
                val minutes = duration.toMinutes().absoluteValue % 60
                val seconds = duration.seconds.absoluteValue % 60
                elapsedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        } else {
            // Reset timer when session ends
            elapsedTime = "00:00:00"
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = elapsedTime,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
} 