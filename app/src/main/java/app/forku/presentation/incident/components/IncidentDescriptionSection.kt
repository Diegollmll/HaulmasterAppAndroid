package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.forku.presentation.incident.IncidentReportState
import coil.compose.AsyncImage
import getImmediateActionsByType

@Composable
fun IncidentDescriptionSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Description
        OutlinedTextField(
            value = state.description,
            onValueChange = { onValueChange(state.copy(description = it)) },
            label = { Text("Narrative Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 8.dp),
            minLines = 4
        )

        // Photos Section
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

    }
} 