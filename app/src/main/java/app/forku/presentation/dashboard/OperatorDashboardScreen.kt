package app.forku.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.forku.presentation.navigation.Screen
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OperatorDashboardScreen(
    navController: NavController? = null,
    onNavigate: (String) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Operator Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )

        // Sección de Vehículo Actual
        if (dashboardState.currentSession != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Current Vehicle",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    dashboardState.displayVehicle?.let { vehicle ->
                        Text("Vehicle: ${vehicle.codename}")
                        Text("Status: ${vehicle.status}")
                    }
                    Button(
                        onClick = { viewModel.endCurrentSession() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("End Session")
                    }
                }
            }
        }

        // Sección de Inicio de Sesión
        if (dashboardState.currentSession == null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Start Session",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onNavigate(Screen.QRScanner.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scan Vehicle QR")
                    }
                }
            }
        }

        // Sección de Reportes de Incidentes
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Incident Reporting",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onNavigate(Screen.SafetyReporting.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Report Incident")
                }
            }
        }

        // Sección de Historial
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigate(Screen.IncidentsHistory.route) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Incidents")
                    }
                    Button(
                        onClick = { onNavigate(Screen.OperatorsCICOHistory.route) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sessions")
                    }
                }
            }
        }
    }
} 