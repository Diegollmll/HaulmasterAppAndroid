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
import app.forku.presentation.common.components.CustomOutlinedTextField
import app.forku.presentation.common.components.FormFieldDivider
import app.forku.presentation.incident.IncidentReportState
import app.forku.presentation.incident.IncidentReportViewModel.UploadedPhoto
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
        CustomOutlinedTextField(
            value = state.description,
            onValueChange = { onValueChange(state.copy(description = it)) },
            label = "Narrative Description *",
            minLines = 3,
            isError = state.attemptedSubmit && state.description.isBlank(),
            supportingText = if (state.attemptedSubmit && state.description.isBlank()) {
                { Text("Please provide a description of the incident") }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            preventLineBreaks = true
        )

        FormFieldDivider()

        // Photos Section
        Text(
            text = "Photos/Evidence",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Photo grid
        val itemSize = 120.dp
        val rows = (state.uploadedPhotos.size + 4) / 3 // +1 por el botón de añadir, redondeado hacia arriba
        val gridHeight = itemSize * rows

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(state.uploadedPhotos) { photo ->
                AsyncImage(
                    model = photo.uri,
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