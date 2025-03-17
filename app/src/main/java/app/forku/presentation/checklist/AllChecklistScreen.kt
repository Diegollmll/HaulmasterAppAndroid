package app.forku.presentation.checklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.checklist.CheckStatus
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AllChecklistScreen(
    navController: NavController,
    viewModel: AllChecklistViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager
) {
    val state by viewModel.state.collectAsState()

    BaseScreen(
        navController = navController,
        showTopBar = true,
        showBottomBar = false,
        topBarTitle = "All Checks",
        networkManager = networkManager,
        onRefresh = { viewModel.loadChecks() }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.checks.isEmpty()) {
                Text(
                    text = "No checks found",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.checks) { check ->
                        CheckCard(
                            check = check,
                            onClick = {
                                navController.navigate(Screen.CheckDetail.createRoute(check.id))
                            }
                        )
                    }
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CheckCard(
    check: PreShiftCheckState,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vehicle: ${check.vehicleId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Operator: ${check.operatorName}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                StatusChip(status = check.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Last updated: ${formatDateTime(check.lastCheckDateTime)}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
public fun StatusChip(status: String) {
    val statusEnum = try {
        CheckStatus.valueOf(status)
    } catch (e: IllegalArgumentException) {
        null
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when (statusEnum) {
            CheckStatus.COMPLETED_PASS -> Color(0xFF4CAF50)
            CheckStatus.COMPLETED_FAIL -> Color.Red
            CheckStatus.IN_PROGRESS -> Color(0xFFFFA726)
            else -> Color.Gray
        }.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = when (statusEnum) {
                    CheckStatus.COMPLETED_PASS -> Color(0xFF4CAF50)
                    CheckStatus.COMPLETED_FAIL -> Color.Red
                    CheckStatus.IN_PROGRESS -> Color(0xFFFFA726)
                    else -> Color.Gray
                },
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = statusEnum?.toFriendlyString() ?: status,
                color = when (statusEnum) {
                    CheckStatus.COMPLETED_PASS -> Color(0xFF4CAF50)
                    CheckStatus.COMPLETED_FAIL -> Color.Red
                    CheckStatus.IN_PROGRESS -> Color(0xFFFFA726)
                    else -> Color.Gray
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatDateTime(dateTime: String): String {
    return try {
        val instant = Instant.parse(dateTime)
        val formatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateTime
    }
} 