package app.forku.presentation.checklist

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
            .padding(vertical = 12.dp),
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
        ) {
            Text(
                text = "Pass",
                fontSize = 16.sp,
                color = if (question.userAnswer == Answer.PASS) 
                    Color(0xFF007AFF) else Color.Gray,
                modifier = Modifier
                    .clickable { onResponseChanged(question.id, true) }
                    .padding(horizontal = 16.dp)
            )
            
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp),
                color = Color.LightGray
            )
            
            Text(
                text = "Fail",
                fontSize = 16.sp,
                color = if (question.userAnswer == Answer.FAIL) 
                    Color(0xFF007AFF) else Color.Gray,
                modifier = Modifier
                    .clickable { onResponseChanged(question.id, false) }
                    .padding(horizontal = 16.dp)
            )
        }
    }
    
    Divider(
        modifier = Modifier.fillMaxWidth(),
        color = Color.LightGray
    )
} 