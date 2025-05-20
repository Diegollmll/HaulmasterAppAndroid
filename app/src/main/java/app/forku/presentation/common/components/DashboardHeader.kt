package app.forku.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AccountCircle
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
    modifier: Modifier = Modifier,
    showNotifications: Boolean = true,
    showProfile: Boolean = false,
    greeting: String = "How are you today?"
) {
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
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = greeting,
                color = Color.Gray,
                fontSize = 18.sp
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showProfile) {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            if (showNotifications) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
} 