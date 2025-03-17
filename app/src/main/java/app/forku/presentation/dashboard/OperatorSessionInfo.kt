package app.forku.presentation.dashboard

import app.forku.domain.model.user.UserRole


data class OperatorSessionInfo(
    val name: String,
    val image: String?,
    val isActive: Boolean,
    val userId: String,
    val sessionStartTime: String,
    val role: UserRole
) 