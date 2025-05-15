package app.forku.presentation.safety

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.core.auth.TokenErrorHandler
import app.forku.presentation.common.components.BaseScreen
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.checklist.PreShiftCheck
import java.time.format.DateTimeFormatter
import java.time.OffsetDateTime
import app.forku.presentation.common.utils.getRelativeTimeSpanString

@Composable
fun SafetyAlertsScreen(
    navController: NavController,
    viewModel: SafetyAlertsViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsState()

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        topBarTitle = "Safety Alerts",
        showBackButton = true,
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.safetyAlerts) { alert ->
                    SafetyAlertItem(alert = alert)
                }

                if (state.safetyAlerts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No safety alerts found",
                                color = Color.Gray,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun SafetyAlertItem(alert: SafetyAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = alert.vehicleCodename,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = getRelativeTimeSpanString(alert.date),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Text(
                text = alert.description,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            
            Text(
                text = "Reported by: ${alert.operatorName}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
} 