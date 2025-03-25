package app.forku.presentation.user.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.user.profile.components.ProfileSections
import app.forku.presentation.user.profile.components.StatsGrid
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import app.forku.presentation.common.utils.getRelativeTimeSpanString
import app.forku.R
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import app.forku.domain.model.user.UserRole
import app.forku.presentation.common.components.OptionsDropdownMenu
import app.forku.presentation.common.components.DropdownMenuOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    operatorId: String? = null,
    onNavigateToCicoHistory: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Load operator profile if operatorId is provided
    LaunchedEffect(operatorId) {
        if (operatorId != null) {
            viewModel.loadOperatorProfile(operatorId)
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes, Logout")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = if (operatorId != null) "Operator Profile" else "User Profile",
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    ProfileHeader(
                        state = state,
                        navController = navController,
                        viewModel = viewModel,
                        isCurrentUser = operatorId == null
                    )
                    StatsGrid(state)
                    ProfileSections(
                        state = state,
                        onQualificationsClick = { /* Navigate to qualifications */ },
                        onIncidentReportsClick = {
                            // For admins viewing their own profile or operators, navigate to filtered incidents
                            if (state.user?.role == UserRole.ADMIN && operatorId == null) {
                                // Admin viewing their own profile - show all incidents
                                navController.navigate(Screen.IncidentList.route)
                            } else {
                                // Viewing specific operator's incidents or operator viewing their own incidents
                                val userId = operatorId ?: state.user?.id
                                navController.navigate(Screen.IncidentList.createRoute(userId = userId, source = "profile"))
                            }
                        },
                        onTrainingRecordClick = { /* Navigate to training */ },
                        onCicoHistoryClick = {
                            // Get the user ID for navigation
                            val userId = when {
                                // If we're viewing another operator's profile, use their ID
                                operatorId != null -> operatorId
                                // If we're in our own profile (admin or operator), use our ID
                                else -> state.user?.id
                            }
                            
                            // Navigate to CICO History with the appropriate source
                            navController.navigate(Screen.OperatorsCICOHistory.createRoute(
                                operatorId = userId,
                                source = if (operatorId == null) "profile" else "operator_profile"
                            ))
                        },
                        isCurrentUser = operatorId == null
                    )
                }
            }
        },
        networkManager = networkManager,
        topBarActions = if (operatorId == null) {
            {
                OptionsDropdownMenu(
                    options = listOf(
                        DropdownMenuOption(
                            text = "Logout",
                            onClick = { showLogoutDialog = true },
                            leadingIcon = Icons.Default.ExitToApp,
                            iconTint = MaterialTheme.colorScheme.error
                        )
                    ),
                    isEnabled = true
                )
            }
        } else null
    )
}

@Composable
private fun ProfileHeader(
    state: ProfileState,
    navController: NavController,
    viewModel: ProfileViewModel,
    isCurrentUser: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Image Section
                    Box(modifier = Modifier.weight(0.4f)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.LightGray, CircleShape)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(state.user?.photoUrl?.takeIf { it.isNotEmpty() }
                                            ?: "https://ui-avatars.com/api/?name=${state.user?.firstName?.first() ?: "U"}+${state.user?.lastName?.first() ?: "U"}&background=random")
                                        .crossfade(true)
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                        .error(R.drawable.ic_profile_placeholder)
                                        .build(),
                                    contentDescription = "Profile picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (state.user?.isActive == true) Color.Green 
                                            else Color.Gray,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.user?.isActive == true) "Active" else "Inactive",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = "Last log:",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                                color = Color.LightGray
                            )
                            Text(
                                text = state.user?.lastLogin?.let { getRelativeTimeSpanString(it) } ?: "N/A",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                color = Color.Gray
                            )
                        }
                    }

                    // Info Section
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = state.user?.role?.name ?: "Guest",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = state.user?.fullName ?: "",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Level",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = state.user?.experienceLevel ?: "Rookie",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Points",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${state.user?.points ?: 0}pts",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Column {
                                Text(
                                    text = "Hours",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${state.user?.totalHours ?: 0}h",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Column {
                                Text(
                                    text = "Distance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${state.user?.totalDistance ?: 0}km",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Button(
                        onClick = {
                            navController.navigate(Screen.PerformanceReport.route)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA726)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Performance Report")
                    }
                }
            }
        }
    }
} 