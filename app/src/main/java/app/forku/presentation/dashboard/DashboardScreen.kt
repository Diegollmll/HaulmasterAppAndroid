package app.forku.presentation.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.presentation.navigation.Screen
import app.forku.presentation.common.components.BaseScreen
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.dashboard.components.SessionCard
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import app.forku.presentation.common.components.DashboardHeader
import app.forku.presentation.common.components.FeedbackBanner
import androidx.compose.foundation.lazy.items
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.presentation.session.SessionViewModel
import app.forku.domain.model.vehicle.Vehicle

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isConnected by networkManager.isConnected.collectAsState()
    val sessionState = sessionViewModel.state.collectAsState().value
    
    var isCheckoutLoading by remember { mutableStateOf(false) }

    // Handle loading state during checkout
    LaunchedEffect(dashboardState.currentSession) {
        if (dashboardState.currentSession == null && isCheckoutLoading) {
            isCheckoutLoading = false
        }
    }

    // Handle errors
    LaunchedEffect(dashboardState.error) {
        dashboardState.error?.let {
            isCheckoutLoading = false
        }
    }

    // Initial load - only trigger on user change
    LaunchedEffect(currentUser?.id) {
        if (currentUser != null) {
            viewModel.refresh()
        }
    }

    // Handle network connectivity changes with debounce
    LaunchedEffect(isConnected) {
        if (isConnected) {
            // Add delay to prevent rapid refreshes
            kotlinx.coroutines.delay(1000)
            viewModel.refresh()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = dashboardState.isLoading,
        onRefresh = { viewModel.refreshWithLoading() }
    )

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    BaseScreen(
        navController = navController,
        showBottomBar = true,
        showTopBar = false,
        showBackButton = false,
        currentVehicleId = dashboardState.currentSession?.vehicleId,
        currentCheckId = dashboardState.lastPreShiftCheck?.id,
        dashboardState = dashboardState,
        networkManager = networkManager,
        onRefresh = null  // Explicitly set to null to prevent auto-refresh on resume
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DashboardHeader(
                            userName = currentUser?.firstName ?: "",
                            onNotificationClick = { navController.navigate(Screen.Notifications.route) }
                        )
                    }

                    // Show current user's session
                    item {
                        CurrentUserSession(
                            dashboardState = dashboardState,
                            currentUser = currentUser,
                            onNavigate = onNavigate,
                            sessionViewModel = sessionViewModel
                        )
                    }

                    // Show navigation buttons
                    item {
                        DashboardNavigationButtons(
                            navController = navController,
                            hasActiveSession = dashboardState.currentSession != null,
                            viewModel = viewModel,
                            isCheckoutLoading = isCheckoutLoading,
                            onCheckoutLoadingChange = { isCheckoutLoading = it }
                        )
                    }
                    
                    item {
                        FeedbackBanner(
                            onFeedbackSubmitted = { rating, feedback ->
                                viewModel.submitFeedback(rating, feedback)
                            }
                        )
                        
                        // Show success message when feedback is submitted
                        if (dashboardState.feedbackSubmitted) {
                            Snackbar(
                                modifier = Modifier.padding(16.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text("Thank you for your feedback!")
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            PullRefreshIndicator(
                refreshing = dashboardState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun CurrentUserSession(
    dashboardState: DashboardState,
    currentUser: User?,
    onNavigate: (String) -> Unit,
    sessionViewModel: SessionViewModel
) {
    // Get current user's session
    val userSession = remember(dashboardState.activeSessions, currentUser?.id) {
        dashboardState.activeSessions.find { session ->
            session.userId == currentUser?.id
        }
    }

    // Get the vehicle for the user's session
    val sessionVehicle = remember(userSession, dashboardState.vehicles) {
        userSession?.let { session ->
            dashboardState.vehicles.find { it.id == session.vehicleId }
        }
    }

    // Show only the current user's session if it exists
    if (sessionVehicle != null && userSession != null) {
        SessionCard(
            vehicle = sessionVehicle,
            lastCheck = dashboardState.checks.find { it.vehicleId == sessionVehicle.id },
            user = currentUser,
            currentSession = userSession,
            onCheckClick = { checkId ->
                onNavigate(Screen.CheckDetail.createRoute(checkId))
            },
            currentUserRole = currentUser?.role ?: UserRole.OPERATOR,
            onEndSession = { sessionId ->
                sessionViewModel.endSession(
                    sessionId = sessionId,
                    isAdminClosure = true
                )
            }
        )
    } else {
        Text("Press Check In to get started!", modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun DashboardNavigationButtons(
    navController: NavController,
    hasActiveSession: Boolean,
    viewModel: DashboardViewModel,
    isCheckoutLoading: Boolean,
    onCheckoutLoadingChange: (Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    // Keep expanded if there's an active session
    LaunchedEffect(hasActiveSession) {
        if (hasActiveSession) {
            isExpanded = true
        }
    }

    val offsetAnimation by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(0.dp)
    ) {
        val circleSize by animateFloatAsState(
            targetValue = if (isExpanded) 560f else 100f,
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = Spring.StiffnessLow
            ),
            label = "circleSize"
        )

        // Decorative outer circle with gradient border and size animation
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(circleSize.dp)
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .padding(3.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
        ){
            // All navigation buttons with animated positioning
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (0 * offsetAnimation).dp)
            ) {
                NavigationButton(
                    icon = Icons.Default.Person,
                    text = "Profile",
                    onClick = {
                        isExpanded = false
                        navController.navigate(Screen.Profile.route)
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(offsetAnimation),
                    backgroundColor = Color.Transparent,
                    showBorder = false,
                    elevation = 0.dp,
                    iconSize = 64.dp,
                    textSize = if (isExpanded) 14.sp else 10.sp
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-0 * offsetAnimation).dp)
            ) {
                NavigationButton(
                    icon = Icons.Default.ClearAll,
                    text = "Fleet",
                    onClick = {
                        isExpanded = false
                        navController.navigate(Screen.VehiclesList.route)
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(offsetAnimation),
                    backgroundColor = Color.Transparent,
                    showBorder = false,
                    elevation = 0.dp,
                    iconSize = 64.dp,
                    textSize = if (isExpanded) 14.sp else 10.sp
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-0 * offsetAnimation).dp)
            ) {
                NavigationButton(
                    icon = Icons.Default.History,
                    text = "CICO",
                    onClick = {
                        isExpanded = false
                        navController.navigate(Screen.OperatorsCICOHistory.route)
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(offsetAnimation),
                    backgroundColor = Color.Transparent,
                    showBorder = false,
                    elevation = 0.dp,
                    iconSize = 64.dp,
                    textSize = if (isExpanded) 14.sp else 10.sp
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (0 * offsetAnimation).dp)
            ) {
                NavigationButton(
                    icon = Icons.Default.Report,
                    text = "Incidents",
                    onClick = {
                        isExpanded = false
                        navController.navigate(Screen.IncidentList.createRoute())
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(offsetAnimation),
                    backgroundColor = Color.Transparent,
                    showBorder = false,
                    elevation = 0.dp,
                    iconSize = 64.dp,
                    textSize = if (isExpanded) 14.sp else 10.sp
                )
            }


        }


        // Center button (QR Scanner or Add)
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .size(120.dp)
                .clickable { 
                    if (hasActiveSession) {
                        onCheckoutLoadingChange(true)
                        viewModel.endCurrentSession()
                    } else {
                        navController.navigate(Screen.QRScanner.route)
                    }
                },
            shape = CircleShape,
            color = if (!isExpanded && !hasActiveSession) 
                Color.White.copy(alpha = 0.9f)
            else 
                MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (!isExpanded && !hasActiveSession) 0.5f else 0.3f)
            ),
            shadowElevation = if (!isExpanded && !hasActiveSession) 0.dp else 4.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val iconAlpha by animateFloatAsState(
                    targetValue = if (!isExpanded && !hasActiveSession) 0.6f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "iconAlpha"
                )
                
                Icon(
                    imageVector = if (hasActiveSession) Icons.Default.Close else Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = iconAlpha.coerceIn(0f, 1f)),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (hasActiveSession) "Check Out" else "Check In",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = iconAlpha.coerceIn(0f, 1f))
                )
            }
        }
    }
}

@Composable
private fun NavigationButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isCenter: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    showBorder: Boolean = true,
    borderWidth: Dp = 1.dp,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    elevation: Dp? = null,
    contentColor: Color? = null,
    buttonSize: Dp = 120.dp,
    iconSize: Dp = 32.dp,
    textSize: TextUnit = if (isCenter) 12.sp else 10.sp
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val finalBorderColor = borderColor ?: if (isCenter) 
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    else 
        tint.copy(alpha = 0.2f)

    val finalBackgroundColor = backgroundColor ?: if (isCenter) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
    else 
        MaterialTheme.colorScheme.surface

    val finalContentColor = contentColor ?: if (isCenter) 
        MaterialTheme.colorScheme.primary
    else 
        tint

    val finalElevation = elevation ?: if (isCenter) 6.dp else 2.dp
    
    Surface(
        modifier = modifier
            .size(buttonSize)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        color = finalBackgroundColor,
        border = if (showBorder) BorderStroke(
            width = borderWidth,
            color = finalBorderColor
        ) else null,
        shadowElevation = finalElevation
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(if (isCenter) 8.dp else 4.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = finalContentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = textSize
                ),
                color = finalContentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CommonContent() {
    // Implementation of CommonContent
}

@Composable
private fun VehicleOperationSection() {
    // Implementation of VehicleOperationSection
}


