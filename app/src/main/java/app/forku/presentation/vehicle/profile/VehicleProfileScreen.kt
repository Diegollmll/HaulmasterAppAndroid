package app.forku.presentation.vehicle.profile


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.forku.domain.model.Vehicle

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.vehicle.components.VehicleProfileSummary
import app.forku.presentation.vehicle.components.VehicleQrCode
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleProfileScreen(
    viewModel: VehicleProfileViewModel = hiltViewModel(),
    onStartCheck: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> LoadingOverlay()
                state.error != null -> ErrorScreen(
                    message = state.error!!,
                    onRetry = { viewModel.loadVehicle() }
                )
                state.vehicle != null -> {
                    VehicleProfileContent(
                        vehicle = state.vehicle!!,
                        onStartCheck = onStartCheck
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleProfileContent(
    vehicle: Vehicle,
    onStartCheck: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = vehicle.type.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Serial: ${vehicle.serialNumber}",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            CurrentVehicleProfileSummary()

            Spacer(modifier = Modifier.height(32.dp))

            VehicleQrCode(
                vehicleId = vehicle.id,
                modifier = Modifier.size(256.dp)
            )

            // Extra space at the bottom for the floating button
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Floating button at the bottom
        Button(
            onClick = onStartCheck,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFA726)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text("Start Check")
        }
    }
}

@Composable
private fun CurrentVehicleProfileSummary() {
    VehicleProfileSummary()
}
