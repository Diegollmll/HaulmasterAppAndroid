package app.forku.presentation.certification.list

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.data.api.dto.certification.CertificationMultimediaDto

/**
 * Ejemplo de componente para mostrar multimedia de certificaciones
 * Basado en el patrón de IncidentMultimedia
 */
@Composable
fun CertificationMultimediaSection(
    certificationId: String,
    userId: String,
    viewModel: CertificationsViewModel,
    modifier: Modifier = Modifier
) {
    var multimedia by remember { mutableStateOf<List<CertificationMultimediaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Cargar multimedia cuando se monta el componente
    LaunchedEffect(certificationId) {
        isLoading = true
        viewModel.getCertificationMultimedia(certificationId) { result ->
            multimedia = result
            isLoading = false
        }
    }
    
    Column(modifier = modifier) {
        Text(
            text = "Multimedia (${multimedia.size})",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else if (multimedia.isEmpty()) {
            Text(
                text = "No multimedia found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Mostrar lista de multimedia
            multimedia.forEach { item ->
                CertificationMultimediaItem(
                    multimedia = item,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botón para agregar multimedia (ejemplo)
        Button(
            onClick = {
                // Ejemplo de agregar multimedia
                // En una implementación real, esto abriría un selector de archivos
                viewModel.addCertificationMultimedia(
                    certificationId = certificationId,
                    userId = userId,
                    imageInternalName = "example_image_${System.currentTimeMillis()}.jpg",
                    imageFileSize = 1024000, // 1MB example
                    multimediaType = 1
                ) { success ->
                    if (success) {
                        // Recargar multimedia
                        viewModel.getCertificationMultimedia(certificationId) { result ->
                            multimedia = result
                        }
                    }
                }
            }
        ) {
            Text("Add Multimedia (Example)")
        }
    }
}

@Composable
fun CertificationMultimediaItem(
    multimedia: CertificationMultimediaDto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Image: ${multimedia.imageInternalName ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Size: ${multimedia.imageFileSize ?: 0} bytes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Type: ${multimedia.multimediaType ?: 0}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            multimedia.creationDateTime?.let { date ->
                Text(
                    text = "Created: $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 