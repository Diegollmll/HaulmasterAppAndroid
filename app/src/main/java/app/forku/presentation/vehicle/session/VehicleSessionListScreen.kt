package app.forku.presentation.vehicle.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.vehicle.toColor
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.components.ErrorScreen
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VehicleSessionListScreen(
    navController: NavController,
    viewModel: VehicleSessionListViewModel = hiltViewModel(),
    onVehicleClick: (String) -> Unit,
    networkManager: NetworkConnectivityManager
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading && state.isRefreshing,
        onRefresh = { viewModel.loadVehicles(true) }
    )

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Vehicles",
        showBottomBar = true,
        onRefresh = { viewModel.loadVehicles(true) },
        showLoadingOnRefresh = false,
        networkManager = networkManager
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                state.error != null -> ErrorScreen(
                    message = state.error!!,
                    onRetry = { viewModel.loadVehicles(true) }
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.vehicles) { vehicleInfo ->
                            VehicleSessionListItem(
                                vehicleInfo = vehicleInfo,
                                onClick = { onVehicleClick(vehicleInfo.vehicle.id) }
                            )
                        }
                    }
                }
            }

            if (state.isLoading && state.isRefreshing) {
                PullRefreshIndicator(
                    refreshing = true,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun VehicleSessionListItem(
    vehicleInfo: VehicleWithSessionInfo,
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
            modifier = Modifier.padding(12.dp)
        ) {
            // Vehicle info section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vehicle Image
                AsyncImage(
                    model = vehicleInfo.vehicle.photoModel,
                    contentDescription = "Vehicle image",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Vehicle Info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Vehicle Name
                    Text(
                        text = vehicleInfo.vehicle.codename,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
//                    // Vehicle Type
//                    Text(
//                        text = vehicleInfo.vehicle.type.displayName,
//                        color = Color.Gray,
//                        fontSize = 12.sp,
//                        maxLines = 1
//                    )
//
                    // ID and Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
//                        // ID
//                        Text(
//                            text = "ID-${vehicleInfo.vehicle.id.take(3)}",
//                            color = Color.Gray,
//                            fontSize = 12.sp
//                        )
//
                        // Status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(vehicleInfo.vehicle.status.toColor(), CircleShape)
                            )
                            Text(
                                text = vehicleInfo.vehicle.status.name,
                                color = vehicleInfo.vehicle.status.toColor(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Session info if active
            vehicleInfo.activeSession?.let { session ->
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Operator Image
                    AsyncImage(
                        model = session.operatorImage,
                        contentDescription = "Operator photo",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.operatorName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Active Session",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
} 