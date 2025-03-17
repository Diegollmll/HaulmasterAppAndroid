package app.forku.presentation.common.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.withStyle

@Composable
fun LinkText(
    text: String,
    linkText: String,
    url: String,
    modifier: Modifier = Modifier,
    shouldOpenInBrowser: Boolean = true
) {
    val context = LocalContext.current
    
    val annotatedText = buildAnnotatedString {
        // Add the regular text
        append(text)
        append(" ")
        
        // Add the link text with styling
        pushStringAnnotation(
            tag = "URL",
            annotation = url
        )
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(linkText)
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    if (shouldOpenInBrowser) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                        context.startActivity(intent)
                    }
                    // Here you can add else condition to handle internal navigation if needed
                }
        },
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}

// Overload for simple link without preceding text
@Composable
fun LinkText(
    linkText: String,
    url: String,
    modifier: Modifier = Modifier,
    shouldOpenInBrowser: Boolean = true
) {
    LinkText(
        text = "",
        linkText = linkText,
        url = url,
        modifier = modifier,
        shouldOpenInBrowser = shouldOpenInBrowser
    )
} 