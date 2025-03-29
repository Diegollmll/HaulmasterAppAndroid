package app.forku.core.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView

@Composable
fun Modifier.hideKeyboardOnTapOutside(): Modifier {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    return this.pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                focusManager.clearFocus()
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                val activity = context as? Activity
                activity?.currentFocus?.let { focusedView ->
                    inputMethodManager?.hideSoftInputFromWindow(focusedView.windowToken, 0)
                }
            }
        )
    }
}

@Composable
fun Modifier.keyboardAwareScroll(scrollState: androidx.compose.foundation.ScrollState): Modifier {
    return this
}

@Composable
private fun keyboardAsState(): State<Boolean> {
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(imeVisible)
} 