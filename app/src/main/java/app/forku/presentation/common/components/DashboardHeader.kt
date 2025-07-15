package app.forku.presentation.common.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardHeader(
    userName: String,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onSettingsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showNotifications: Boolean = true,
    showProfile: Boolean = false,
    greeting: String = "How are you today?"
) {
    Log.d("DashboardHeader", "DashboardHeader created - onSettingsClick is ${if (onSettingsClick != null) "NOT NULL" else "NULL"}")
    
    val buttonCount = listOf(showProfile, showNotifications, onSettingsClick != null).count { it }
    Log.d("DashboardHeader", "Total buttons to show: $buttonCount (Profile: $showProfile, Notifications: $showNotifications, Settings: ${onSettingsClick != null})")
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hi, $userName!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = greeting,
                color = Color.Gray,
                fontSize = 16.sp,
                maxLines = 1
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showProfile) {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (showNotifications) {
                Log.d("DashboardHeader", "Notifications button will be shown")
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Log.d("DashboardHeader", "Notifications button will NOT be shown")
            }
            if (onSettingsClick != null) {
                Log.d("DashboardHeader", "Settings button will be shown")
                IconButton(
                    onClick = {
                        Log.d("DashboardHeader", "Settings button clicked")
                        onSettingsClick()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "System Settings",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Log.d("DashboardHeader", "Settings button will NOT be shown - onSettingsClick is null")
            }
        }
    }
} 