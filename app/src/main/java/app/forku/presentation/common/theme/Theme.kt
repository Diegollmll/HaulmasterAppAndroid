package app.forku.presentation.common.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.res.colorResource
import app.forku.R

@Composable
fun ForkUTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val DarkColorScheme = darkColorScheme(
        primary = colorResource(id = R.color.primary_blue),
        onPrimary = Color.White,
        secondary = colorResource(id = R.color.primary_blue),
        onSecondary = Color.White,
        tertiary = colorResource(id = R.color.primary_blue),
        onTertiary = Color.White,
        background = colorResource(id = R.color.background_gray),
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black
    )

    val LightColorScheme = lightColorScheme(
        primary = colorResource(id = R.color.primary_blue),
        onPrimary = Color.White,
        secondary = colorResource(id = R.color.primary_blue),
        onSecondary = Color.White,
        tertiary = colorResource(id = R.color.primary_blue),
        onTertiary = Color.White,
        background = colorResource(id = R.color.background_gray),
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black
    )

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}