package app.forku.presentation.business.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.forku.domain.model.user.User
import app.forku.presentation.dashboard.Business

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignSuperAdminDialog(
    business: Business,
    availableSuperAdmins: List<User>,
    currentSuperAdmin: User?,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign SuperAdmin to ${business.name}") },
        text = {
            Column {
                // "None" option to explicitly deassign SuperAdmin
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAssign("") }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSuperAdmin == null,
                        onClick = { onAssign("") }
                    )
                    Text(
                        text = "None (No SuperAdmin)",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                if (currentSuperAdmin != null) {
                    Text(
                        text = "Current SuperAdmin:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (availableSuperAdmins.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No SuperAdmins available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column {
                        availableSuperAdmins.forEach { superAdmin ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAssign(superAdmin.id) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentSuperAdmin?.id == superAdmin.id,
                                    onClick = { onAssign(superAdmin.id) }
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "${superAdmin.firstName} ${superAdmin.lastName}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = superAdmin.email,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 