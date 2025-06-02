package app.forku.presentation.common.imageloader

import androidx.compose.runtime.staticCompositionLocalOf
import coil.ImageLoader

val LocalAuthenticatedImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("No ImageLoader provided")
} 