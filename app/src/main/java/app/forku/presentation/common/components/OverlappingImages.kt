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
import coil.ImageLoader
import app.forku.core.Constants.BASE_URL
import app.forku.presentation.common.utils.getUserAvatarData
import app.forku.presentation.common.components.UserAvatar
import androidx.compose.ui.unit.sp
import android.util.Log

@Composable
fun OverlappingImages(
    mainImageUrl: String?,
    overlayImageUrl: String?,
    mainTint: Color = Color.Gray,
    overlayBackground: Color = Color.White,
    mainSize: Int = 40,
    overlaySize: Int = 20,
    imageLoader: ImageLoader,
    overlayUserId: String? = null,
    overlayFirstName: String? = null,
    overlayLastName: String? = null
) {
    Log.d("OverlappingImages", "Function parameters:")
    Log.d("OverlappingImages", "mainImageUrl=$mainImageUrl")
    Log.d("OverlappingImages", "overlayImageUrl=$overlayImageUrl") 
    Log.d("OverlappingImages", "mainTint=$mainTint")
    Log.d("OverlappingImages", "overlayBackground=$overlayBackground")
    Log.d("OverlappingImages", "mainSize=$mainSize")
    Log.d("OverlappingImages", "overlaySize=$overlaySize")
    Log.d("OverlappingImages", "overlayUserId=$overlayUserId")
    Log.d("OverlappingImages", "overlayFirstName=$overlayFirstName")
    Log.d("OverlappingImages", "overlayLastName=$overlayLastName")
    val userImageUrl = if (!overlayImageUrl.isNullOrBlank()) {
        overlayImageUrl
    } else if (!overlayUserId.isNullOrBlank()) {
        "${BASE_URL}api/gouser/file/${overlayUserId}/Picture?t=%LASTEDITEDTIME%"
    } else null

    var imageFailed = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Log.d("OverlappingImages", "Rendering overlay: overlayImageUrl=$overlayImageUrl, overlayFirstName=$overlayFirstName, overlayLastName=$overlayLastName, userImageUrl=$userImageUrl")
    Box(
        modifier = Modifier
            .size(mainSize.dp + (overlaySize.dp / 2))
            .padding(4.dp)
    ) {
        Log.d("OverlappingImages", "mainImageUrl=$mainImageUrl, overlayFirstName=$overlayFirstName, overlayLastName=$overlayLastName")
        // Main vehicle/user image or fallback
        if (!mainImageUrl.isNullOrBlank()) {
            Log.d("OverlappingImages", "Rendering main image: $mainImageUrl")
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(mainImageUrl)
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = "Vehicle image",
                modifier = Modifier
                    .size(mainSize.dp)
                    .align(Alignment.TopStart)
                    .clip(CircleShape)
                    .border(1.dp, mainTint, CircleShape),
                contentScale = ContentScale.Crop,
                onSuccess = {
                    Log.d("OverlappingImages", "Imagen principal cargada correctamente: $mainImageUrl")
                },
                onError = {
                    Log.e("OverlappingImages", "Error al cargar imagen principal: $mainImageUrl")
                }
            )
        } else {
            Log.d("OverlappingImages", "Rendering initials fallback for: $overlayFirstName $overlayLastName")
            Box(
                modifier = Modifier
                    .size(mainSize.dp)
                    .align(Alignment.TopStart)
                    .clip(CircleShape)
                    .border(1.dp, mainTint, CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                UserAvatar(
                    avatarData = getUserAvatarData(overlayFirstName, overlayLastName, null),
                    size = mainSize.dp,
                    fontSize = (mainSize / 2).sp
                )
            }
        }
        // Overlay user image or initials
        if (!userImageUrl.isNullOrBlank() && !imageFailed.value) {
            Log.d("OverlappingImages", "Intentando cargar imagen overlay: $userImageUrl para usuario: $overlayFirstName $overlayLastName (userId: $overlayUserId)")
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userImageUrl)
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = "User image",
                modifier = Modifier
                    .size(overlaySize.dp)
                    .align(Alignment.BottomEnd)
                    .zIndex(1f)
                    .clip(CircleShape)
                    .background(overlayBackground)
                    .border(1.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop,
                onSuccess = {
                    Log.d("OverlappingImages", "Imagen overlay cargada correctamente: $userImageUrl para usuario: $overlayFirstName $overlayLastName (userId: $overlayUserId)")
                },
                onError = {
                    Log.e("OverlappingImages", "Error al cargar imagen overlay: $userImageUrl para usuario: $overlayFirstName $overlayLastName (userId: $overlayUserId)")
                    imageFailed.value = true
                }
            )
        } else if (overlaySize > 0) {
            val avatarData = getUserAvatarData(overlayFirstName, overlayLastName, null)
            Log.d("OverlappingImages", "Rendering UserAvatar initials: ${avatarData.initials}, firstName: $overlayFirstName, lastName: $overlayLastName")
            Box(
                modifier = Modifier
                    .size(overlaySize.dp)
                    .align(Alignment.BottomEnd)
                    .zIndex(1f)
                    .clip(CircleShape)
                    .background(overlayBackground)
                    .border(1.dp, Color.White, CircleShape)
            ) {
                UserAvatar(
                    avatarData = avatarData,
                    size = overlaySize.dp,
                    fontSize = (overlaySize / 2).sp
                )
            }
        }
    }
    Log.d("OverlappingImages", "Overlay rendering complete.")
} 