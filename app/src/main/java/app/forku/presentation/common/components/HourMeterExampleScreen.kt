package app.forku.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Example screen demonstrating HourMeterIncrementer usage
 * This shows different scenarios and configurations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourMeterExampleScreen() {
    var currentHourMeter by remember { mutableStateOf("1234.5") }
    var sessionInitialHourMeter by remember { mutableStateOf("1234.5") }
    var sessionFinalHourMeter by remember { mutableStateOf("1234.5") }
    var showDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Hour Meter Components Demo",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Example 1: Vehicle Hour Meter Update
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Vehicle Hour Meter Update",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                HourMeterIncrementer(
                    currentValue = currentHourMeter,
                    minValue = currentHourMeter,
                    onValueChange = { currentHourMeter = it },
                    title = "Vehicle Hour Meter",
                    subtitle = "Update the vehicle's current hour meter reading",
                    incrementStep = 0.1
                )
            }
        }
        
        // Example 2: Session Initial Hour Meter
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Session Initial Hour Meter",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                HourMeterIncrementer(
                    currentValue = sessionInitialHourMeter,
                    minValue = currentHourMeter,
                    onValueChange = { sessionInitialHourMeter = it },
                    title = "Initial Session Reading",
                    subtitle = "Enter hour meter reading when starting vehicle session",
                    incrementStep = 0.5,
                    showIncrementButtons = true
                )
            }
        }
        
        // Example 3: Session Final Hour Meter
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Session Final Hour Meter",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                HourMeterIncrementer(
                    currentValue = sessionFinalHourMeter,
                    minValue = sessionInitialHourMeter,
                    onValueChange = { sessionFinalHourMeter = it },
                    title = "Final Session Reading",
                    subtitle = "Enter hour meter reading when ending vehicle session",
                    incrementStep = 1.0,
                    showIncrementButtons = true
                )
            }
        }
        
        // Example 4: Compact Version (no increment buttons)
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. Compact Version",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                HourMeterIncrementer(
                    currentValue = currentHourMeter,
                    minValue = "0",
                    onValueChange = { /* Read-only for demo */ },
                    title = "Hour Meter Reading",
                    subtitle = "Simple input without increment buttons",
                    showIncrementButtons = false,
                    enabled = false
                )
            }
        }
        
        // Example 5: Dialog Version
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "5. Dialog Version",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show Hour Meter Dialog")
                }
            }
        }
        
        // Usage Tips
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 Usage Tips",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "• Hour meter values can only increase (never decrease)\n" +
                          "• Quick increment buttons: +0.5h, +1h, +2h, +5h\n" +
                          "• Validation prevents invalid input\n" +
                          "• Visual feedback for validation status\n" +
                          "• Automatic formatting to 1 decimal place",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Current Values Display
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📊 Current Values",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Vehicle Hour Meter: $currentHourMeter hrs")
                Text("Session Initial: $sessionInitialHourMeter hrs")
                Text("Session Final: $sessionFinalHourMeter hrs")
                
                val sessionHours = try {
                    val final = sessionFinalHourMeter.toDouble()
                    val initial = sessionInitialHourMeter.toDouble()
                    if (final >= initial) "${final - initial} hrs" else "Invalid"
                } catch (e: Exception) {
                    "Invalid"
                }
                Text("Session Duration: $sessionHours")
            }
        }
    }
    
    // Example Dialog
    HourMeterIncrementDialog(
        isVisible = showDialog,
        currentValue = currentHourMeter,
        minValue = currentHourMeter,
        title = "Update Hour Meter",
        subtitle = "This is an example of the dialog version",
        onDismiss = { showDialog = false },
        onConfirm = { newValue ->
            currentHourMeter = newValue
            showDialog = false
        }
    )
} 