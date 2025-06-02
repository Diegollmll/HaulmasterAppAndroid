package app.forku.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import coil.compose.AsyncImage
import app.forku.presentation.common.utils.UserAvatarData
import app.forku.presentation.common.imageloader.LocalAuthenticatedImageLoader

@Composable
fun UserAvatar(
    avatarData: UserAvatarData,
    size: Dp = 32.dp,
    fontSize: TextUnit = 14.sp
) {
    val imageLoader = LocalAuthenticatedImageLoader.current
    if (!avatarData.imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = avatarData.imageUrl,
            imageLoader = imageLoader,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(avatarData.backgroundColor.takeIf { it != Color.Unspecified } ?: Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarData.initials,
                color = Color(0xFF222222),
                fontSize = (size.value / 2).sp,
                maxLines = 1
            )
        }
    }
} 