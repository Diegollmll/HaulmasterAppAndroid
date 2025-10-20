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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import app.forku.core.Constants.BASE_URL
import app.forku.core.auth.TokenErrorHandler
import app.forku.domain.model.user.UserRole
import app.forku.presentation.common.components.OptionsDropdownMenu
import app.forku.presentation.common.components.DropdownMenuOption
import app.forku.presentation.common.components.OverlappingImages
import coil.compose.LocalImageLoader
import app.forku.presentation.common.utils.getUserAvatarData
import app.forku.presentation.common.components.UserAvatar
import coil.ImageLoader
import app.forku.data.datastore.AuthDataStore
import kotlinx.coroutines.runBlocking


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    operatorId: String? = null,
    onNavigateToCicoHistory: () -> Unit,
    tokenErrorHandler: TokenErrorHandler,
    imageLoader: ImageLoader
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isAdminRole = state.user?.role == UserRole.SYSTEM_OWNER || state.user?.role == UserRole.SUPERADMIN
    
    // --- Get tokens for image URL ---
    val context = LocalContext.current
    val authDataStore = remember { AuthDataStore(context) }
    var appToken by remember { mutableStateOf<String?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        appToken = authDataStore.getApplicationToken()
        authToken = authDataStore.getAuthenticationToken()
        android.util.Log.d("ProfileScreen", "Loaded tokens for image: appToken=${appToken?.take(10)}, authToken=${authToken?.take(10)}")
    }

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
        topBarTitle = when {
            operatorId != null -> "Driver Profile"
            isAdminRole -> "${state.user?.role?.name ?: ""} Profile"
            else -> "User Profile"
        },
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
                        isCurrentUser = operatorId == null,
                        imageLoader = imageLoader,
                        appToken = appToken,
                        authToken = authToken
                    )

                    // Stats grid is only relevant for operational roles, not admin roles
                    if (!isAdminRole) {
                        StatsGrid(state)
                    }

                    ProfileSections(
                        state = state,
                        onCertificationsClick = {
                            // Get the user ID for navigation
                            val userId = when {
                                // If we're viewing another operator's profile, use their ID
                                operatorId != null -> operatorId
                                // If we're in our own profile, use our ID
                                else -> state.user?.id
                            }
                            
                            // Navigate to Certifications with the appropriate user ID
                            if (userId != null) {
                                navController.navigate(Screen.CertificationsList.createRoute(userId = userId))
                            } else {
                                android.util.Log.w("ProfileScreen", "Cannot navigate to certifications: no valid userId available")
                            }
                        },
                        onIncidentReportsClick = {
                            // For admins viewing their own profile or operators, navigate to filtered incidents
                            if (state.user?.role == UserRole.ADMIN && operatorId == null) {
                                // Admin viewing their own profile - show all incidents
                                navController.navigate(Screen.IncidentList.route)
                            } else {
                                // Viewing specific operator's incidents or operator viewing their own incidents
                                val userId = operatorId ?: state.user?.id
                                // Only navigate if we have a valid userId
                                if (userId != null) {
                                navController.navigate(Screen.IncidentList.createRoute(userId = userId, source = "profile"))
                                } else {
                                    // Log warning if no userId is available
                                    android.util.Log.w("ProfileScreen", "Cannot navigate to incidents: no valid userId available")
                                }
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
                            if (userId != null) {
                            navController.navigate(Screen.OperatorsCICOHistory.createRoute(
                                operatorId = userId,
                                source = if (operatorId == null) "profile" else "operator_profile"
                            ))
                            } else {
                                android.util.Log.w("ProfileScreen", "Cannot navigate to CICO history: no valid userId available")
                            }
                        },
                        isCurrentUser = operatorId == null,
                        navController = navController
                    )
                }
            }
        },
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler,
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
    isCurrentUser: Boolean,
    imageLoader: ImageLoader,
    appToken: String?,
    authToken: String?
) {
    val isAdminRole = state.user?.role == UserRole.SYSTEM_OWNER || state.user?.role == UserRole.SUPERADMIN
    
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
                            state.user?.let { user ->
                                // --- Build authenticated image URL ---
                                android.util.Log.d("ProfileHeader", "user=${user}")
                                android.util.Log.d("ProfileHeader", "user.photoUrl=${user.photoUrl}")
                                val baseUrl = user.photoUrl?.takeIf { it.isNotBlank() }
                                android.util.Log.d("ProfileHeader", "baseUrl=$baseUrl")
                                val authenticatedUrl = if (!baseUrl.isNullOrBlank() && appToken != null && authToken != null) {
                                    val url = "$baseUrl&_application_token=$appToken&_user_token=$authToken"
                                    android.util.Log.d("ProfileHeader", "Authenticated user image URL: $url")
                                    url
                                } else baseUrl
                                android.util.Log.d("ProfileHeader", "mainImageUrl=$authenticatedUrl")
                                OverlappingImages(
                                    mainImageUrl = authenticatedUrl,
                                    overlayImageUrl = null,
                                    mainTint = MaterialTheme.colorScheme.onSurface,
                                    mainSize = 100,
                                    overlaySize = 0,
                                    imageLoader = imageLoader,
                                    overlayUserId = null,
                                    overlayFirstName = user.firstName,
                                    overlayLastName = user.lastName
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
                            Row {
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
                            text = when {
                                !state.user?.fullName.isNullOrBlank() -> state.user?.fullName!!
                                !state.user?.username.isNullOrBlank() -> state.user?.username!!
                                else -> "No Name"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Business and Site Context - Show for all users (moved from bottom)
                        if (state.currentBusinessName != null || state.currentSiteName != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Current Context",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                
                                state.currentBusinessName?.let { businessName ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "Business: ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = businessName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                
                                state.currentSiteName?.let { siteName ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                ) {
                                    Text(
                                            text = "Site: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                            text = siteName,
                                        style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        } else if (!isAdminRole) {
                            // Show a placeholder for operational users without context
                            Text(
                                text = "No business context set",
                                style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                        } else {
                            // For admin roles, show administrator info
                            Text(
                                text = "Administrator Account",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
} 