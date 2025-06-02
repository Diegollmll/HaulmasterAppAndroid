package app.forku.presentation.common.utils

import androidx.compose.ui.graphics.Color
import app.forku.domain.model.user.User

data class UserAvatarData(
    val imageUrl: String?,
    val initials: String,
    val backgroundColor: Color = Color(0xFFE0E0E0)
)

fun getUserAvatarData(
    firstName: String?,
    lastName: String?,
    photoUrl: String?
): UserAvatarData {
    val initials = listOfNotNull(
        firstName?.firstOrNull()?.toString()?.uppercase(),
        lastName?.firstOrNull()?.toString()?.uppercase()
    ).joinToString("").ifBlank { "??" }

    return if (!photoUrl.isNullOrBlank()) {
        UserAvatarData(imageUrl = photoUrl, initials = initials)
    } else {
        UserAvatarData(imageUrl = null, initials = initials)
    }
}

fun getUserAvatarData(user: User?): UserAvatarData =
    getUserAvatarData(user?.firstName, user?.lastName, user?.photoUrl) 