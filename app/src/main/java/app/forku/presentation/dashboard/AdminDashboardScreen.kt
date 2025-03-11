package app.forku.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.forku.presentation.navigation.Screen

@Composable
fun AdminDashboardScreen(
    navController: NavController? = null,
    onNavigate: (String) -> Unit = {},
    viewModel: DashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )

        // Sección de Gestión de Usuarios
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "User Management",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onNavigate("manage_users") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage Users")
                }
            }
        }

        // Sección de Gestión de Vehículos
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Vehicle Management",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onNavigate(Screen.Vehicles.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage Vehicles")
                }
            }
        }

        // Sección de Reportes
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Reports",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onNavigate("reports") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Reports")
                }
            }
        }

        // Sección de Certificaciones
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Certifications",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onNavigate("certifications") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage Certifications")
                }
            }
        }
    }
} 