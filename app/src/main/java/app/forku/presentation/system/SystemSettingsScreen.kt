package app.forku.presentation.system

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import android.util.Log
import app.forku.core.auth.TokenErrorHandler

@Composable
fun SystemSettingsScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "System settings",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
//            item {
//                Text(
//                    text = "General Settings",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//            }

//            item {
//                SettingsCard {
////                    SettingsItem(
////                        icon = Icons.Default.Language,
////                        title = "Countries",
////                        subtitle = "Manage country settings and configurations",
////                        onClick = { navController.navigate(Screen.Countries.route) }
////                    )
////                    Divider()
////                    SettingsItem(
////                        icon = Icons.Default.Schedule,
////                        title = "Time Zones",
////                        subtitle = "Configure system time zones",
////                        onClick = { navController.navigate(Screen.TimeZones.route) }
////                    )
////                    Divider()
////                    SettingsItem(
////                        icon = Icons.Default.EnergySavingsLeaf,
////                        title = "Energy Sources",
////                        subtitle = "Configure system energy sources",
////                        onClick = { navController.navigate(Screen.EnergySources.route) }
////                    )
//                }
//            }


/*
            item {
                Text(
                    text = "Vehicle Management",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
*/
            //item {
                //SettingsCard {
//                    SettingsItem(
//                        icon = Icons.Default.Category,
//                        title = "Vehicle Categories",
//                        subtitle = "Manage vehicle categories",
//                        onClick = { navController.navigate(Screen.VehicleCategories.route) }
//                    )
//                    Divider()
//                    SettingsItem(
//                        icon = Icons.Default.DirectionsCar,
//                        title = "Vehicle Types",
//                        subtitle = "Manage vehicle types",
//                        onClick = { navController.navigate(Screen.VehicleTypes.route) }
//                    )
//                    Divider()
//                    SettingsItem(
//                        icon = Icons.Default.Build,
//                        title = "Vehicle Components",
//                        subtitle = "Manage vehicle components",
//                        onClick = { navController.navigate(Screen.VehicleComponents.route) }
//                    )
//                    Divider()

                    /*
                    SettingsItem(
                        icon = Icons.Default.AddCircle,
                        title = "Create Vehicle",
                        subtitle = "Add a new vehicle to the system",
                        onClick = { navController.navigate(Screen.AddVehicle.route) }
                    )
                    */
               // }
            //}
            
            // Sección para manejo de checklists
            item {
                Text(
                    text = "Checklist Management",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingsCard {
//                    SettingsItem(
//                        icon = Icons.Default.CheckBox,
//                        title = "Checklist Item Categories ",
//                        subtitle = "Manage questionary checklist item categories",
//                        onClick = { navController.navigate(Screen.ChecklistCategories.route) }
//                    )
//                    Divider()
//                    SettingsItem(
//                        icon = Icons.Default.List,
//                        title = "Checklist Item Subcategories",
//                        subtitle = "Manage questionary checklist item subcategories",
//                        onClick = { navController.navigate(Screen.ChecklistSubcategories.route) }
//                    )
//                    Divider()
                    SettingsItem(
                        icon = Icons.Default.Assignment,
                        title = "Checklists",
                        subtitle = "Create and edit checklist questionnaire rules",
                        onClick = { 
                            Log.d("SystemSettingsScreen", "✅ Checklists button clicked - navigating to: ${Screen.Questionnaires.route}")
                            navController.navigate(Screen.Questionnaires.route)
                        }
                    )
//                    Divider()
//                    SettingsItem(
//                        icon = Icons.Default.QuestionAnswer,
//                        title = "Questionary Items",
//                        subtitle = "Manage individual checklist questions",
//                        onClick = { navController.navigate(Screen.QuestionaryItems.route) }
//                    )
                }
            }

            // Sección para preferencias del usuario
            item {
                Text(
                    text = "User Preferences",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Default.Settings,
                        title = "Setup User Preferences",
                        subtitle = "Configure business and site preferences",
                        onClick = { navController.navigate(Screen.UserPreferencesSetup.createRoute(showBack = true)) }
                    )
                }
            }

            // Sección para reportes y análisis
            item {
                Text(
                    text = "Reports & Analytics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Default.Analytics,
                        title = "System Reports",
                        subtitle = "Generate and export comprehensive system reports",
                        onClick = {
                            Log.d("SystemSettingsScreen", "System Reports button clicked")
                            navController.navigate(Screen.Reports.route)
                        }
                    )
                }
            }

//            item {
//                Text(
//                    text = "Security",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//            }

//            item {
//                SettingsCard {
//                    SettingsItem(
//                        icon = Icons.Default.Security,
//                        title = "Authentication",
//                        subtitle = "Configure authentication settings",
//                        onClick = { /* TODO: Implement */ }
//                    )
//                    Divider()
//                    SettingsItem(
//                        icon = Icons.Default.Lock,
//                        title = "Password Policy",
//                        subtitle = "Manage password requirements",
//                        onClick = { /* TODO: Implement */ }
//                    )
//                }
//            }

//            item {
//                Text(
//                    text = "Maintenance",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//            }

//            item {
//                SettingsCard {
//                    SettingsItem(
//                        icon = Icons.Default.Storage,
//                        title = "Database",
//                        subtitle = "Manage database settings",
//                        onClick = { /* TODO: Implement */ }
//                    )
//                    Divider()
//                    SettingsItem(
//                        icon = Icons.Default.Build,
//                        title = "System Maintenance",
//                        subtitle = "Schedule and manage maintenance",
//                        onClick = { /* TODO: Implement */ }
//                    )
//                }
//            }
            

            }
        }
    }
}

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
} 