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
    sessionStartTime: LocalDateTime? = null,
    isSessionActive: Boolean = true,
    fontSize: Int = 34
) {
    var dateTime by remember { mutableStateOf(initialDateTime) }
    var elapsedTime by remember(sessionStartTime, isSessionActive) { mutableStateOf("00:00:00") }
    
    LaunchedEffect(sessionStartTime, isSessionActive) {
        if (sessionStartTime != null && isSessionActive) {
            while(true) {
                try {
                    val now = LocalDateTime.now()
                    val duration = java.time.Duration.between(sessionStartTime, now)
                    val hours = duration.toHours().absoluteValue
                    val minutes = duration.toMinutes().absoluteValue % 60
                    val seconds = duration.seconds.absoluteValue % 60
                    
                    elapsedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    delay(1000)
                } catch (e: Exception) {
                    android.util.Log.e("UserDateTimer", "Error calculating duration", e)
                    delay(1000)
                }
            }
        } else {
            // If session is not active, calculate final duration
            if (sessionStartTime != null) {
                try {
                    val now = LocalDateTime.now()
                    val duration = java.time.Duration.between(sessionStartTime, now)
                    val hours = duration.toHours().absoluteValue
                    val minutes = duration.toMinutes().absoluteValue % 60
                    val seconds = duration.seconds.absoluteValue % 60
                    elapsedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                } catch (e: Exception) {
                    android.util.Log.e("UserDateTimer", "Error calculating final duration", e)
                }
            } else {
                elapsedTime = "00:00:00"
            }
        }
    }

    Column(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = elapsedTime,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = fontSize.sp,
            modifier = Modifier.height(IntrinsicSize.Min)
        )
    }
} 