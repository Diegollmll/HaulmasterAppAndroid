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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.background

@Composable
fun ImageUploader(
    images: List<Uri>,
    onAddImage: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Photos", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(images.size) { index ->
                Box(modifier = Modifier.size(80.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(images[index]),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(2.dp)
                    )
                    IconButton(
                        onClick = { onRemoveImage(images[index]) },
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