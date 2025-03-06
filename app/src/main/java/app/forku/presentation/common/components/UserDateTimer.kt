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

@Composable
fun UserDateTimer(
    modifier: Modifier = Modifier,
    initialDateTime: LocalDateTime = LocalDateTime.now(),
    sessionStartTime: LocalDateTime? = null
) {
    var dateTime by remember { mutableStateOf(initialDateTime) }
    var elapsedTime by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        while(true) {
            delay(1000)
            dateTime = LocalDateTime.now()
            sessionStartTime?.let {
                val duration = java.time.Duration.between(it, dateTime)
                val hours = duration.toHours()
                val minutes = duration.toMinutes() % 60
                val seconds = duration.seconds % 60
                elapsedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        sessionStartTime?.let {
            Text(
                text = "$elapsedTime",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Text(
            text = dateTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
} 