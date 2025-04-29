package app.forku.presentation.business.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.domain.model.business.BusinessStatus
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.presentation.dashboard.Business
import app.forku.presentation.business.components.BusinessStatusChip
import app.forku.presentation.business.components.AssignSuperAdminDialog

@Composable
fun BusinessCard(
    business: Business,
    currentUser: User?,
    currentSuperAdmin: User?,
    availableSuperAdmins: List<User>,
    onAssignSuperAdmin: (String) -> Unit,
    onUpdateStatus: (BusinessStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAssignSuperAdminDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Business Name and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = business.name,
                    style = MaterialTheme.typography.titleLarge
                )
                BusinessStatusChip(
                    status = business.status,
                    onStatusChange = { if (currentUser?.role == UserRole.SYSTEM_OWNER) onUpdateStatus(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // SuperAdmin Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SuperAdmin",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (currentSuperAdmin != null) {
                        Text(
                            text = "${currentSuperAdmin.firstName} ${currentSuperAdmin.lastName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Not assigned",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // Only show assign button for SYSTEM_OWNER or if current user is the SuperAdmin
                if (currentUser?.role == UserRole.SYSTEM_OWNER || 
                    (currentSuperAdmin?.id == currentUser?.id)) {
                    Button(
                        onClick = { showAssignSuperAdminDialog = true },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(text = if (currentSuperAdmin == null) "Assign" else "Change")
                    }
                }
            }
        }
    }
    
    if (showAssignSuperAdminDialog) {
        AssignSuperAdminDialog(
            business = business,
            availableSuperAdmins = availableSuperAdmins,
            currentSuperAdmin = currentSuperAdmin,
            onDismiss = { showAssignSuperAdminDialog = false },
            onAssign = { superAdminId ->
                onAssignSuperAdmin(superAdminId)
                showAssignSuperAdminDialog = false
            }
        )
    }
} 