package app.forku.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun OverlappingImages(
    mainImageUrl: String?,
    overlayImageUrl: String?,
    mainTint: Color = Color.Gray,
    overlayBackground: Color = Color.White,
    mainSize: Int = 40,
    overlaySize: Int = 20
) {
    Box(
        modifier = Modifier
            .size(mainSize.dp + (overlaySize.dp / 2))
            .padding(4.dp)
    ) {
        // Main vehicle image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(mainImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Vehicle image",
            modifier = Modifier
                .size(mainSize.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .border(1.dp, mainTint, CircleShape),
            contentScale = ContentScale.Crop
        )
        
        // Overlay user image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(overlayImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "User image",
            modifier = Modifier
                .size(overlaySize.dp)
                .align(Alignment.BottomEnd)
                .zIndex(1f)
                .clip(CircleShape)
                .background(overlayBackground)
                .border(1.dp, Color.White, CircleShape),
            contentScale = ContentScale.Crop
        )
    }
} 