package app.forku.presentation.checklist

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.Answer
import app.forku.presentation.common.components.ImageUploader
import app.forku.presentation.checklist.model.ChecklistImage
import androidx.compose.animation.AnimatedVisibility

@Composable
fun ChecklistQuestionItem(
    question: ChecklistItem,
    onResponseChanged: (String, Boolean) -> Unit,
    onDescriptionToggled: (Boolean) -> Unit = {},
    onCommentChanged: (String, String) -> Unit = { _, _ -> },
    images: List<ChecklistImage>,
    onAddImage: () -> Unit,
    onRemoveImage: (ChecklistImage) -> Unit,
    modifier: Modifier = Modifier,
    uploadingImages: Set<Uri> = emptySet(),
    imageLoader: coil.ImageLoader? = null
) {
    var showDescription by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf(question.userComment ?: "") }

    // Log when component is recomposed
    LaunchedEffect(question.id, question.userAnswer) {
        android.util.Log.d("ChecklistFields", "Question[${question.id}] - userAnswer: ${question.userAnswer}, " +
            "hasComment: ${question.userComment != null}, " +
            "imagesCount: ${images.size}")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Question and buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Question text and info icon
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { 
                            showDescription = !showDescription
                            onDescriptionToggled(showDescription)
                            // android.util.Log.d("ChecklistQuestionItem", "Clicked! New showDescription value: $showDescription")
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = question.question,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    if (question.description.isNotEmpty()) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Info,
                            contentDescription = "More info",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(start = 8.dp)
                        )
                    }
                }

                // Yes/No buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (question.userAnswer == Answer.PASS) 
                            Color(0xFF4CAF50).copy(alpha = 0.2f) 
                        else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (question.userAnswer == Answer.PASS) 
                                Color(0xFF4CAF50) 
                            else Color.LightGray
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Yes",
                            fontSize = 16.sp,
                            color = if (question.userAnswer == Answer.PASS) 
                                Color(0xFF4CAF50)
                            else Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable { onResponseChanged(question.id, true) }
                                .padding(horizontal = 18.dp, vertical = 13.dp)
                        )
                    }
                    
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (question.userAnswer == Answer.FAIL) 
                            Color(0xFFFF5252).copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (question.userAnswer == Answer.FAIL) 
                                Color(0xFFFF5252)
                            else Color.LightGray
                        )
                    ) {
                        Text(
                            text = "No",
                            fontSize = 16.sp,
                            color = if (question.userAnswer == Answer.FAIL) 
                                Color(0xFFFF5252)
                            else Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable { onResponseChanged(question.id, false) }
                                .padding(horizontal = 18.dp, vertical = 13.dp)
                        )
                    }
                }
            }

            // Description (if visible)
            if (showDescription && question.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = question.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Show comment field and photos ONLY if the question is answered, with animation
            AnimatedVisibility(visible = question.userAnswer != null) {
                Column {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = {
                            android.util.Log.d("ChecklistFields", "Comment changed for Question[${question.id}] - " +
                                "New comment: \${it.take(20)}...")
                            comment = it
                            onCommentChanged(question.id, it)
                        },
                        label = { Text("Comment (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5))
                            .padding(vertical = 4.dp),
                        singleLine = false,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        android.util.Log.d("ChecklistFields", "Rendering ImageUploader for Question[${question.id}] - " +
                            "Total images: \${images.size}, " +
                            "Uploading: \${uploadingImages.size}")
                        ImageUploader(
                            images = images,
                            onAddImage = onAddImage,
                            onRemoveImage = onRemoveImage,
                            imageLoader = imageLoader
                        )
                    }
                }
            }
        }
        
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray
        )
    }
} 