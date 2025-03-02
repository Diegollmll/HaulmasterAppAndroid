package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import app.forku.presentation.incident.IncidentReportState

@Composable
fun DocumentationSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        // Photos
        Text(
            text = "Photos/Evidence",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Photo grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(200.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(state.photos) { photoUri ->
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Incident photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                )
            }
            
            item {
                FilledTonalIconButton(
                    onClick = onAddPhoto,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.Add, "Add photo")
                }
            }
        }

        // Final review text
        Text(
            text = "Please review all information before submitting",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
} 