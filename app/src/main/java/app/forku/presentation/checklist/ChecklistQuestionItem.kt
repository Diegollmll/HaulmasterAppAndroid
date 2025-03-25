package app.forku.presentation.checklist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.Answer

@Composable
fun ChecklistQuestionItem(
    question: ChecklistItem,
    onResponseChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = question.question,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )
        
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
    
    Divider(
        modifier = Modifier.fillMaxWidth(),
        color = Color.LightGray
    )
} 