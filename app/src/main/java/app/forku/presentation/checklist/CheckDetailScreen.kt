package app.forku.presentation.checklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.forku.core.auth.TokenErrorHandler
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.utils.getRelativeTimeSpanString


@Composable
fun CheckDetailScreen(
    checkId: String,
    navController: NavController,
    viewModel: CheckDetailViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(checkId) {
        viewModel.loadCheckDetail(checkId)
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        showBottomBar = false,
        topBarTitle = "Check Details",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
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
            } else {
                state.check?.let { check ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Check Information",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                        StatusChip(status = check.status)
                                    }

                                    Divider()

                                    InfoRow(
                                        label = "Vehicle",
                                        check.vehicleCodename
                                    )
                                    InfoRow(
                                        label = "Operator",
                                        check.operatorName
                                    )
                                    InfoRow(
                                        label = "Date",
                                        value = check.lastCheckDateTime?.let { getRelativeTimeSpanString(it) } ?: "Not available",
                                        valueColor = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Add more sections for check details as needed
                    }
                } ?: run {
                    Text(
                        text = state.error ?: "Check not found",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
} 