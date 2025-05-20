package app.forku.presentation.user.operator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import app.forku.core.auth.TokenErrorHandler
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.dashboard.OperatorSessionInfo
import app.forku.presentation.navigation.Screen
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OperatorsListScreen(
    navController: NavController,
    viewModel: OperatorsListViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading && state.isRefreshing,
        onRefresh = { viewModel.loadOperators(true) }
    )

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Users",
        showBottomBar = false,
        onRefresh = { viewModel.loadOperators(true) },
        showLoadingOnRefresh = false,
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
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
                    onRetry = { viewModel.loadOperators(true) }
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.operators) { operator ->
                            OperatorSessionListItem(
                                operator = operator,
                                onClick = {
                                    navController.navigate(Screen.Profile.createRoute(operator.userId))
                                }
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
private fun OperatorSessionListItem(
    operator: OperatorSessionInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box {
                AsyncImage(
                    model = operator.image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                if (operator.isActive) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                            .align(Alignment.BottomEnd)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = when {
                        operator.fullName.isNotBlank() -> operator.fullName
                        operator.username.isNotBlank() -> operator.username
                        else -> operator.name
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = operator.role.toString().capitalize(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 