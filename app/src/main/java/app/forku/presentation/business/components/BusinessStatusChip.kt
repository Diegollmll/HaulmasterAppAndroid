package app.forku.presentation.business.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.forku.domain.model.business.BusinessStatus

@Composable
fun BusinessStatusChip(
    status: BusinessStatus,
    onStatusChange: ((BusinessStatus) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    AssistChip(
        onClick = { 
            if (onStatusChange != null) {
                showStatusMenu = true
            }
        },
        label = { Text(status.name) },
        leadingIcon = {
            Icon(
                imageVector = when (status) {
                    BusinessStatus.ACTIVE -> Icons.Default.CheckCircle
                    BusinessStatus.PENDING -> Icons.Default.Pending
                    BusinessStatus.SUSPENDED -> Icons.Default.Block
                },
                contentDescription = null,
                tint = when (status) {
                    BusinessStatus.ACTIVE -> Color(0xFF4CAF50)
                    BusinessStatus.PENDING -> Color(0xFFFFA726)
                    BusinessStatus.SUSPENDED -> Color(0xFFF44336)
                },
                modifier = Modifier.size(18.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = when (status) {
                BusinessStatus.ACTIVE -> Color(0xFFE8F5E9)
                BusinessStatus.PENDING -> Color(0xFFFFF3E0)
                BusinessStatus.SUSPENDED -> Color(0xFFFFEBEE)
            }
        ),
        trailingIcon = if (onStatusChange != null) {
            {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Change status",
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        modifier = modifier
    )

    if (showStatusMenu && onStatusChange != null) {
        DropdownMenu(
            expanded = showStatusMenu,
            onDismissRequest = { showStatusMenu = false }
        ) {
            BusinessStatus.values().forEach { newStatus ->
                DropdownMenuItem(
                    text = { Text(newStatus.name) },
                    onClick = {
                        onStatusChange(newStatus)
                        showStatusMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (newStatus) {
                                BusinessStatus.ACTIVE -> Icons.Default.CheckCircle
                                BusinessStatus.PENDING -> Icons.Default.Pending
                                BusinessStatus.SUSPENDED -> Icons.Default.Block
                            },
                            contentDescription = null,
                            tint = when (newStatus) {
                                BusinessStatus.ACTIVE -> Color(0xFF4CAF50)
                                BusinessStatus.PENDING -> Color(0xFFFFA726)
                                BusinessStatus.SUSPENDED -> Color(0xFFF44336)
                            }
                        )
                    }
                )
            }
        }
    }
} 