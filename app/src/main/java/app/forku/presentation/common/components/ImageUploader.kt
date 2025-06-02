package app.forku.presentation.common.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.background
import app.forku.presentation.checklist.model.ChecklistImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember

@Composable
fun ImageUploader(
    images: List<ChecklistImage>,
    onAddImage: () -> Unit,
    onRemoveImage: (ChecklistImage) -> Unit,
    modifier: Modifier = Modifier,
    imageLoader: coil.ImageLoader? = null
) {
    Column(modifier = modifier) {
        Text("Photos", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(images.size) { index ->
                val checklistImage = images[index]
                android.util.Log.d("ImageUploader", "[Render] Rendering image with uri: ${checklistImage.uri}")
                val context = LocalContext.current
                val request = remember(checklistImage.uri, imageLoader) {
                    val builder = ImageRequest.Builder(context)
                        .data(checklistImage.uri)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .listener(
                            onSuccess = { request, result ->
                                val response = result.dataSource
                                android.util.Log.d("ImageUploader", "[Coil][SUCCESS] Loaded image uri=${checklistImage.uri}, dataSource=$response")
                            },
                            onError = { request, result ->
                                android.util.Log.e("ImageUploader", "[Coil][ERROR] Failed to load image uri=${checklistImage.uri}, error=${result.throwable}")
                            }
                        )
                    builder.build()
                }
                val painter = if (imageLoader != null) {
                    rememberAsyncImagePainter(
                        model = request,
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                        error = painterResource(id = android.R.drawable.ic_delete),
                        imageLoader = imageLoader
                    )
                } else {
                    rememberAsyncImagePainter(
                        model = request,
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                        error = painterResource(id = android.R.drawable.ic_delete)
                    )
                }
                Box(modifier = Modifier.size(80.dp)) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(2.dp)
                    )
                    if (checklistImage.isUploading) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.5f))
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = { onRemoveImage(checklistImage) },
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.TopEnd)
                            .size(24.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }
            item {
                IconButton(
                    onClick = onAddImage,
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Image", tint = Color.Gray)
                }
            }
        }
    }
}

// Utility to build an authenticated ImageLoader for Coil with CSRF and Cookie headers
fun buildAuthenticatedImageLoader(
    context: android.content.Context,
    csrfToken: String,
    cookie: String,
    applicationToken: String? = null,
    authenticationToken: String? = null
): coil.ImageLoader {
    val cookieHeader = buildString {
        append(cookie)
        if (!applicationToken.isNullOrBlank()) {
            append("; ApplicationToken=")
            append(applicationToken)
            append("; BearerToken=")
            append(applicationToken)
        }
        if (!authenticationToken.isNullOrBlank()) {
            append("; AuthenticationToken=")
            append(authenticationToken)
        }
    }
    val client = okhttp3.OkHttpClient.Builder()
        .addInterceptor(object : okhttp3.Interceptor {
            override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
                val request = chain.request().newBuilder()
                    .addHeader("X-CSRF-TOKEN", csrfToken)
                    .addHeader("Cookie", cookieHeader)
                    .addHeader("Accept", "*/*")
                    .addHeader("accept", "*/*")
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("User-Agent", System.getProperty("http.agent") ?: "Android-Coil")
                    .build()
                return chain.proceed(request)
            }
        })
        .build()

    return coil.ImageLoader.Builder(context)
        .okHttpClient(client)
        .build()
} 