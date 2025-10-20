package app.forku.presentation.user.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.user.profile.ProfileState
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.Alignment
import app.forku.domain.model.user.UserRole
import androidx.navigation.NavController
import app.forku.presentation.navigation.Screen

@Composable
fun ProfileSections(
    state: ProfileState,
    onCertificationsClick: () -> Unit,
    onIncidentReportsClick: () -> Unit,
    onTrainingRecordClick: () -> Unit,
    onCicoHistoryClick: () -> Unit,
    isCurrentUser: Boolean = true,
    navController: NavController
) {
    val isAdminRole = state.user?.role == UserRole.SYSTEM_OWNER || state.user?.role == UserRole.SUPERADMIN
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = if (isCurrentUser) "Profile" else "Driver Information",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        // Certifications are shown for all user roles
        ProfileSection(
            "Certifications",
            onClick = onCertificationsClick
        )

        // Incident Reports are not relevant for System Owners and Super Admins
        if (!isAdminRole) {
            ProfileSection(
                title = "Incident Reports",
                onClick = onIncidentReportsClick
            )
        }

        // CICO History is not relevant for System Owners and Super Admins
        if (!isAdminRole) {
            ProfileSection(
                title = if (isCurrentUser) "My DOCS History" else "DOCS History",
                onClick = onCicoHistoryClick
            )
        }
        
        // Add admin-specific sections for admin roles
        if (isAdminRole) {
            ProfileSection(
                title = "Account Settings",
                onClick = { /* Handle account settings navigation */ }
            )
            
            ProfileSection(
                title = "System Preferences",
                onClick = { navController.navigate(Screen.SystemSettings.route) }
            )
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 