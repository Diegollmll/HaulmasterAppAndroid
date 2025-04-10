package app.forku.presentation.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class DropdownMenuOption(
    val text: String,
    val onClick: () -> Unit,
    val leadingIcon: ImageVector? = null,
    val iconTint: Color = Color.Unspecified,
    val enabled: Boolean = true,
    val adminOnly: Boolean = false
)

@Composable
fun OptionsDropdownMenu(
    options: List<DropdownMenuOption>,
    isEnabled: Boolean = true,
    iconTint: Color = Color(0xFFFFA726)
) {
    var showMenu by remember { mutableStateOf(false) }

    if (isEnabled) {
        Box {
            IconButton(
                onClick = { showMenu = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = iconTint
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                options.forEach { option ->
                    if (option.enabled) {
                        DropdownMenuItem(
                            text = { Text(option.text) },
                            onClick = {
                                showMenu = false
                                option.onClick()
                            },
                            leadingIcon = option.leadingIcon?.let { icon ->
                                {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = option.text,
                                        tint = option.iconTint
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
} 