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
import java.time.Instant
import java.time.Duration

@Composable
fun UserDateTimer(
    modifier: Modifier = Modifier,
    sessionStartInstant: Instant? = null,
    isSessionActive: Boolean = true,
    fontSize: Int = 34
) {
    val currentSessionStartInstant by rememberUpdatedState(sessionStartInstant)
    val currentIsSessionActive by rememberUpdatedState(isSessionActive)
    var elapsedTime by remember { mutableStateOf("00:00:00") }

    LaunchedEffect(currentSessionStartInstant, currentIsSessionActive) {
        if (currentSessionStartInstant != null && currentIsSessionActive) {
            while (true) {
                try {
                    val now = Instant.now()
                    val start = currentSessionStartInstant
                    val duration = Duration.between(start, now)
                    val totalSeconds = duration.seconds
                    if (totalSeconds >= 0) {
                        val hours = totalSeconds / 3600
                        val minutes = (totalSeconds % 3600) / 60
                        val seconds = totalSeconds % 60
                        elapsedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        elapsedTime = "00:00:00"
                    }
                    delay(1000)
                } catch (e: Exception) {
                    delay(1000)
                }
            }
        } else {
            elapsedTime = "00:00:00"
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