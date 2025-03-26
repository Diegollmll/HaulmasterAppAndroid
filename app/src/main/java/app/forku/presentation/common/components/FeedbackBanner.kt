package app.forku.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign

data class FeedbackEmoji(
    val emoji: String,
    val rating: Int
)

private val feedbackEmojis = listOf(
    FeedbackEmoji("😠", 1),
    FeedbackEmoji("🙁", 2),
    FeedbackEmoji("😐", 3),
    FeedbackEmoji("😊", 4),
    FeedbackEmoji("😍", 5)
)

@Composable
fun FeedbackBanner(
    onFeedbackSubmitted: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFeedbackDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showFeedbackDialog = true }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "We’re always looking to improve. \nGive us feedback!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }

    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { rating, feedback ->
                onFeedbackSubmitted(rating, feedback)
                showFeedbackDialog = false
            }
        )
    }
}

@Composable
private fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var selectedRating by remember { mutableStateOf<Int?>(null) }
    var feedbackText by remember { mutableStateOf(TextFieldValue()) }
    var contactEnabled by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "How could we improve?",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "How can we make it better?",
                    style = MaterialTheme.typography.bodyLarge
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    items(feedbackEmojis) { feedbackEmoji ->
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { selectedRating = feedbackEmoji.rating },
                            shape = RoundedCornerShape(24.dp),
                            color = if (selectedRating == feedbackEmoji.rating)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (selectedRating == feedbackEmoji.rating)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = feedbackEmoji.emoji,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Share your thoughts...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = contactEnabled,
                        onCheckedChange = { contactEnabled = it }
                    )
                    Text(
                        text = "I'm happy for Wise to contact me to discuss my feedback.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    selectedRating?.let { rating ->
                        onSubmit(rating, feedbackText.text)
                    }
                },
                enabled = selectedRating != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        },
        modifier = Modifier.padding(16.dp)
    )
} 