package app.forku.presentation.common.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.forku.presentation.navigation.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.presentation.common.viewmodel.BottomSheetViewModel
import app.forku.presentation.incident.components.IncidentTypeSelector

@Composable
fun ForkUBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BottomSheetViewModel = hiltViewModel()
) {
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()

    Column {
        if (showBottomSheet) {
            AppBottomSheet(
                onDismiss = { viewModel.hideBottomSheet() },
                content = {
                    IncidentTypeSelector(
                        onTypeSelected = { type ->
                            navController.navigate("incident_report/$type") {
                                popUpTo("incident_report/$type") { inclusive = true }
                            }
                            viewModel.hideBottomSheet()
                        },
                        onDismiss = { viewModel.hideBottomSheet() }
                    )
                }
            )
        }

        NavigationBar(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            bottomNavItems.forEach { item ->
                NavigationBarItem(
                    selected = item.route == Screen.SafetyReporting.route,
                    onClick = {
                        if (item.route == Screen.SafetyReporting.route) {
                            viewModel.showBottomSheet()
                        } else {
                            navController.navigate(item.route) {
                                popUpTo(item.route) { inclusive = true }
                            }
                        }
                    },
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title) }
                )
            }
        }
    }
}

private val bottomNavItems = listOf(
    BottomNavItem(
        title = "Safety Reporting",
        icon = Icons.Default.Add,
        route = Screen.SafetyReporting.route
    )
)

private data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)