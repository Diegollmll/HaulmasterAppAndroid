package app.forku.presentation.common.components

import androidx.compose.runtime.Composable
import app.forku.domain.model.user.User
import app.forku.domain.model.user.hasAllPermissions
import app.forku.domain.model.user.hasAnyPermission

@Composable
fun PermissionGate(
    user: User?,
    requiredPermissions: Set<String>,
    requireAll: Boolean = true,
    unauthorizedContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (user == null) {
        unauthorizedContent()
        return
    }

    val hasPermission = if (requireAll) {
        user.hasAllPermissions(requiredPermissions)
    } else {
        user.hasAnyPermission(requiredPermissions)
    }

    if (hasPermission) {
        content()
    } else {
        unauthorizedContent()
    }
} 