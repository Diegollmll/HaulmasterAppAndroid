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
fun DateTime(
    modifier: Modifier = Modifier,
    initialDateTime: LocalDateTime = LocalDateTime.now()
) {
    var dateTime by remember { mutableStateOf(initialDateTime) }
    
    LaunchedEffect(Unit) {
        while(true) {
            delay(1000) // Update every second
            dateTime = LocalDateTime.now()
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = dateTime.format(DateTimeFormatter.ofPattern("h:mm a")),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        )
        
        Text(
            text = dateTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
} 