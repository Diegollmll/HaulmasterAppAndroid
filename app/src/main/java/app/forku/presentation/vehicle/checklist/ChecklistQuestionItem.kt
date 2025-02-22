package app.forku.presentation.vehicle.checklist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.forku.domain.model.Answer
import app.forku.domain.model.Checklist
import app.forku.domain.model.ChecklistItem
import app.forku.domain.model.Criticality

@Composable
fun ChecklistQuestionItem(
    question: ChecklistItem,
    onResponseChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (question.criticality == Criticality.CRITICAL)
                Color(0x1FFF0000) else Color(0xFF2D2D2D)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = question.question,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = if (question.criticality == Criticality.CRITICAL)
                    FontWeight.Bold else FontWeight.Normal
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Yes button
                Button(
                    onClick = { onResponseChanged(question.id, true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (question.expectedAnswer == Answer.PASS)
                            Color(0xFF4CAF50) else Color.DarkGray
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Yes")
                }

                // No button
                Button(
                    onClick = { onResponseChanged(question.id, false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (question.expectedAnswer == Answer.FAIL)
                            Color.Red else Color.DarkGray
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("No")
                }
            }
        }
    }
} 