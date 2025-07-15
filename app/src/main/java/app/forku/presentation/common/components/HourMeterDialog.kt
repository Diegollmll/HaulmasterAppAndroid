package app.forku.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.forku.core.validation.HourMeterValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourMeterDialog(
    isVisible: Boolean,
    currentValue: String = "",
    title: String = "Update Hour Meter",
    subtitle: String = "Enter current hour meter reading",
    placeholder: String = "0.0",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean = false,
    allowEqual: Boolean = false // Whether equal values are allowed
) {
    var hourMeterInput by remember(currentValue) { mutableStateOf(currentValue) }
    var validationError by remember { mutableStateOf<String?>(null) }
    
    // Validate input whenever it changes
    val validationResult = remember(hourMeterInput, currentValue) {
        if (hourMeterInput.isBlank()) {
            HourMeterValidator.ValidationResult(false, "Hour meter reading is required")
        } else {
            // Normalize input for validation (replace comma with dot if needed)
            val normalizedInput = hourMeterInput.replace(',', '.')
            HourMeterValidator.validateHourMeterUpdate(
                newValue = normalizedInput,
                currentValue = currentValue,
                allowEqual = allowEqual
            )
        }
    }
    
    val isValid = validationResult.isValid
    
    if (isVisible) {
        AlertDialog(
            onDismissRequest = { 
                if (!isLoading) onDismiss() 
            },
            title = { 
                Text(title) 
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (currentValue.isNotBlank()) {
                        Text(
                            text = "Current reading: $currentValue hrs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    OutlinedTextField(
                        value = hourMeterInput,
                        onValueChange = { 
                            hourMeterInput = it
                            validationError = null
                        },
                        label = { Text("Hour Meter Reading") },
                        placeholder = { Text(placeholder) },
                        suffix = { Text("hrs") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        isError = !isValid && hourMeterInput.isNotBlank(),
                        supportingText = if (!isValid && hourMeterInput.isNotBlank()) {
                            { 
                                Text(
                                    text = validationResult.errorMessage ?: "Invalid input",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                    
                    // Show validation success message
                    if (isValid && hourMeterInput.isNotBlank()) {
                        // Parse values with localization support
                        val currentDouble = try {
                            currentValue.toDoubleOrNull() ?: currentValue.replace(',', '.').toDoubleOrNull() ?: 0.0
                        } catch (e: Exception) { 0.0 }
                        
                        val newDouble = try {
                            hourMeterInput.toDoubleOrNull() ?: hourMeterInput.replace(',', '.').toDoubleOrNull() ?: 0.0
                        } catch (e: Exception) { 0.0 }
                        
                        val difference = newDouble - currentDouble
                        
                        if (difference > 0) {
                            Text(
                                text = "✓ Will increase by ${HourMeterValidator.formatHourMeter(difference)} hrs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (difference == 0.0 && allowEqual) {
                            Text(
                                text = "✓ No change (same value)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    
                    Text(
                        text = "⚠️ Hour meter readings can only increase, never decrease.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (isValid) {
                            // Normalize input before formatting (replace comma with dot)
                            val normalizedInput = hourMeterInput.replace(',', '.')
                            val formattedValue = HourMeterValidator.formatHourMeter(normalizedInput)
                            onConfirm(formattedValue)
                        }
                    },
                    enabled = !isLoading && isValid
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