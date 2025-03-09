package app.forku.presentation.common.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ForkuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFBF00), // Amber Yellow
            contentColor = Color.Black,
            disabledContainerColor = Color(0xFFFFBF00).copy(alpha = 0.5f),
            disabledContentColor = Color.Black.copy(alpha = 0.5f)
        )
    ) {
        content()
    }
} 