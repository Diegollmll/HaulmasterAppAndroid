package app.forku.presentation.common.components

import android.util.Log
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
    allowEqual: Boolean = false
) {
    var hourMeterInput by remember(currentValue) { mutableStateOf(currentValue) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // 🔍 Log when component is composed
    LaunchedEffect(Unit) {
        Log.d("HourMeterDialog", "🔍 Dialog opened with currentValue='$currentValue'")
        if (currentValue.isBlank()) {
            Log.w("HourMeterDialog", "⚠️ currentValue is blank — user may not know current hour meter")
        }
    }

    val validationResult = remember(hourMeterInput, currentValue) {
        if (hourMeterInput.isBlank()) {
            HourMeterValidator.ValidationResult(false, "Hour meter reading is required")
        } else {
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
            onDismissRequest = { if (!isLoading) onDismiss() },
            title = { Text(title) },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    // Mostrar mensaje de éxito si es válido
                    if (isValid && hourMeterInput.isNotBlank()) {
                        val currentDouble = try {
                            currentValue.toDoubleOrNull()
                                ?: currentValue.replace(',', '.').toDoubleOrNull()
                                ?: 0.0
                        } catch (e: Exception) {
                            Log.e("HourMeterDialog", "❌ Error parsing currentValue '$currentValue': ${e.message}")
                            0.0
                        }

                        val newDouble = try {
                            hourMeterInput.toDoubleOrNull()
                                ?: hourMeterInput.replace(',', '.').toDoubleOrNull()
                                ?: 0.0
                        } catch (e: Exception) {
                            Log.e("HourMeterDialog", "❌ Error parsing hourMeterInput '$hourMeterInput': ${e.message}")
                            0.0
                        }

                        Log.d("HourMeterDialog", "📊 Parsed currentDouble=$currentDouble, newDouble=$newDouble")

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
                            val normalizedInput = hourMeterInput.replace(',', '.')
                            val formattedValue = HourMeterValidator.formatHourMeter(normalizedInput)
                            Log.d("HourMeterDialog", "✅ Confirm clicked with value: $formattedValue")
                            onConfirm(formattedValue)
                        }
                    },
                    enabled = !isLoading && isValid
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Update")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !isLoading) {
                    Text("Cancel")
                }
            }
        )
    }
}
