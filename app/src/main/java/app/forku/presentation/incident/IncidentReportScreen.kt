package app.forku.presentation.incident

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.incident.model.IncidentFormSection
import app.forku.presentation.incident.components.IncidentTopBar
import app.forku.presentation.incident.components.IncidentFormContent
import app.forku.presentation.incident.components.LocationHandler
import android.app.Activity
import android.content.IntentSender
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.setValue
import app.forku.presentation.incident.model.InjurySeverity


@Composable
fun IncidentReportScreen(
    incidentType: String,
    onNavigateBack: () -> Unit,
    viewModel: IncidentReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(incidentType) {
        viewModel.setIncidentType(incidentType)
    }

    LocationHandler(
        locationSettingsException = state.locationSettingsException,
        onPermissionsGranted = { viewModel.onLocationPermissionGranted() },
        onPermissionsDenied = { viewModel.onLocationPermissionDenied() }
    )

    Scaffold(
        topBar = {
            IncidentTopBar(
                incidentType = state.type,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        IncidentFormContent(
            state = state,
            onValueChange = { viewModel.updateState(it) },
            modifier = Modifier.padding(paddingValues)
        )
    }

    if (state.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessDialog() },
            title = { Text("Success") },
            text = { Text("Incident report submitted successfully") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissSuccessDialog()
                        onNavigateBack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun InjurySeverityDropdown(
    selected: InjurySeverity,
    onSelected: (InjurySeverity) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.name.replace("_", " "),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            InjurySeverity.values().forEach { severity ->
                DropdownMenuItem(
                    text = { Text(severity.name.replace("_", " ")) },
                    onClick = {
                        onSelected(severity)
                        expanded = false
                    }
                )
            }
        }
    }
}
