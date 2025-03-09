package app.forku.presentation.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    unfocusedBorderColor: Color = if (readOnly || !enabled) Color.Transparent 
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.00f),
    focusedBorderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.07f),
    unfocusedLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLabelColor: Color = MaterialTheme.colorScheme.primary,
    cursorColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    disabledTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        label = label?.let { { Text(it) } },
        minLines = minLines,
        maxLines = maxLines,
        shape = shape,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = focusedBorderColor,
            unfocusedBorderColor = unfocusedBorderColor,
            focusedLabelColor = focusedLabelColor,
            unfocusedLabelColor = unfocusedLabelColor,
            cursorColor = cursorColor,
            focusedTextColor = textColor,
            unfocusedTextColor = unfocusedTextColor,
            disabledTextColor = disabledTextColor,
            disabledBorderColor = Color.Transparent,
            disabledLabelColor = unfocusedLabelColor.copy(alpha = 0.38f)
        )
    )
} 