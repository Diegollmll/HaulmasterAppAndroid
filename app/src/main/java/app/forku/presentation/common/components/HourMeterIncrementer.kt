package app.forku.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Hour Meter Incrementer Component
 * Provides easy-to-use controls for hour meter readings with validation
 * Prevents decrementing below the minimum value
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourMeterIncrementer(
    currentValue: String,
    minValue: String = "0",
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Hour Meter Reading",
    subtitle: String? = null,
    incrementStep: Double = 0.1,
    enabled: Boolean = true,
    showIncrementButtons: Boolean = true,
    isLoading: Boolean = false
) {
    val currentDouble = currentValue.toDoubleOrNull() ?: 0.0
    val minDouble = minValue.toDoubleOrNull() ?: 0.0
    
    var textFieldValue by remember(currentValue) { mutableStateOf(currentValue) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Validate input
    fun validateAndUpdate(newValue: String) {
        // Parse with localization support (handle both comma and dot)
        val newDouble = try {
            newValue.toDoubleOrNull() ?: newValue.replace(',', '.').toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
        
        when {
            newValue.isBlank() -> {
                hasError = false
                errorMessage = ""
                textFieldValue = newValue
            }
            newDouble == null -> {
                hasError = true
                errorMessage = "Invalid number format"
                textFieldValue = newValue
            }
            newDouble < minDouble -> {
                hasError = true
                errorMessage = "Cannot be less than $minValue hrs (current minimum)"
                textFieldValue = newValue
            }
            else -> {
                hasError = false
                errorMessage = ""
                textFieldValue = newValue
                onValueChange(String.format("%.1f", newDouble))
            }
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasError) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title and subtitle
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Show minimum value info
                Text(
                    text = "Minimum allowed: $minValue hrs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            // Current value display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Reading:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currentValue hrs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Input field with increment/decrement buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrement button (disabled if would go below minimum)
                if (showIncrementButtons) {
                    IconButton(
                        onClick = {
                            val newValue = max(minDouble, currentDouble - incrementStep)
                            validateAndUpdate(String.format("%.1f", newValue))
                        },
                        enabled = enabled && !isLoading && currentDouble > minDouble,
                        colors = IconButtonDefaults.iconButtonColors(
                            disabledContentColor = MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrease hour meter",
                            tint = if (currentDouble <= minDouble) {
                                MaterialTheme.colorScheme.outline
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
                
                // Text field (read-only, only buttons can change value)
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { /* Read-only - no manual editing */ },
                    label = { Text("New Reading") },
                    suffix = { Text("hrs") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = false, // Always disabled for manual input
                    readOnly = true, // Explicitly read-only
                    isError = hasError,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    supportingText = if (hasError) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                // Increment button
                if (showIncrementButtons) {
                    IconButton(
                        onClick = {
                            val newValue = currentDouble + incrementStep
                            validateAndUpdate(String.format("%.1f", newValue))
                        },
                        enabled = enabled && !isLoading,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase hour meter"
                        )
                    }
                }
            }
            
            // Quick increment buttons
            if (showIncrementButtons && enabled && !isLoading) {
                Text(
                    text = "Quick Add:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.5, 1.0, 2.0, 5.0).forEach { increment ->
                        OutlinedButton(
                            onClick = {
                                val newValue = currentDouble + increment
                                validateAndUpdate(String.format("%.1f", newValue))
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+${increment}h",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Validation status
            if (!hasError && textFieldValue.isNotBlank()) {
                val newDouble = textFieldValue.toDoubleOrNull()
                if (newDouble != null && newDouble >= minDouble) {
                    val difference = newDouble - currentDouble
                    if (difference > 0) {
                        Text(
                            text = "✓ Will increase by ${String.format("%.1f", difference)} hrs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (difference == 0.0) {
                        Text(
                            text = "No change",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            // Loading indicator
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Updating hour meter...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Simple Hour Meter Dialog using the incrementer
 */
@Composable
fun HourMeterIncrementDialog(
    isVisible: Boolean,
    currentValue: String = "0",
    minValue: String = "0",
    title: String = "Update Hour Meter",
    subtitle: String = "Enter current hour meter reading",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean = false
) {
    var newValue by remember(currentValue) { mutableStateOf(currentValue) }
    val isValid = try {
        val newDouble = newValue.toDoubleOrNull() ?: newValue.replace(',', '.').toDoubleOrNull()
        val minDouble = minValue.toDoubleOrNull() ?: minValue.replace(',', '.').toDoubleOrNull() ?: 0.0
        newDouble?.let { it >= minDouble } == true
    } catch (e: Exception) {
        false
    }
    
    if (isVisible) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) onDismiss() },
            title = { Text(title) },
            text = {
                HourMeterIncrementer(
                    currentValue = currentValue,
                    minValue = minValue,
                    onValueChange = { newValue = it },
                    title = "",
                    subtitle = subtitle,
                    enabled = !isLoading,
                    isLoading = isLoading
                )
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(newValue) },
                    enabled = !isLoading && isValid && newValue != currentValue
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Update")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("Cancel")
                }
            }
        )
    }
} 