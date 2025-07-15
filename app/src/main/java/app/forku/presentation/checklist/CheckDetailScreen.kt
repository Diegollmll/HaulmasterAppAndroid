package app.forku.presentation.checklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Label
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.core.auth.TokenErrorHandler
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.utils.getRelativeTimeSpanString
import app.forku.domain.model.checklist.getPreShiftStatusText
import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.checklist.AnsweredChecklistItem
import app.forku.domain.model.checklist.CheckStatus
import app.forku.data.api.dto.checklist.ChecklistItemAnswerMultimediaDto
import app.forku.core.Constants
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CheckDetailScreen(
    checkId: String,
    navController: NavController,
    viewModel: CheckDetailViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(checkId) {
        viewModel.loadCheckDetail(checkId)
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        showBottomBar = false,
        topBarTitle = "Check Details",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                state.check?.let { check ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Check Information Section
                        item {
                            CheckInformationCard(
                                check = check,
                                checklistAnswer = state.checklistAnswer
                            )
                        }

                        // Vehicle Details Section
                        state.vehicle?.let { vehicle ->
                            item {
                                VehicleDetailsCard(
                                    vehicle = vehicle,
                                    vehicleType = state.vehicleType,
                                    vehicleCategory = state.vehicleCategory,
                                    site = state.site
                                )
                            }
                        }

                        // Checklist Information Section
                        state.checklist?.let { checklist ->
                            item {
                                ChecklistInformationCard(checklist = checklist)
                            }
                        }

                        // Answered Items Section
                        if (state.answeredItems.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Questions & Answers (${state.answeredItems.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            items(state.answeredItems) { answeredItem ->
                                AnsweredItemCard(
                                    answeredItem = answeredItem,
                                    multimedia = state.multimediaByAnswerId[answeredItem.id] ?: emptyList()
                                )
                            }
                        }
                    }
                } ?: run {
                    Text(
                        text = state.error ?: "Check not found",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckInformationCard(
    check: PreShiftCheckState,
    checklistAnswer: app.forku.domain.model.checklist.ChecklistAnswer?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Check Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusChip(status = getPreShiftStatusText(check.status?.toIntOrNull()))
            }

            HorizontalDivider()

            InfoRowWithIcon(
                icon = Icons.Default.Person,
                label = "Operator",
                value = check.operatorName,
                valueColor = MaterialTheme.colorScheme.onSurface
            )
            
            InfoRowWithIcon(
                icon = Icons.Default.Schedule,
                label = "Date",
                value = check.lastCheckDateTime?.let { getRelativeTimeSpanString(it) } ?: "Not available",
                valueColor = MaterialTheme.colorScheme.primary
            )

            checklistAnswer?.let { answer ->
                if (answer.startDateTime.isNotBlank()) {
                    InfoRowWithIcon(
                        icon = Icons.Default.Schedule,
                        label = "Start Time",
                        value = formatDateTime(answer.startDateTime),
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (answer.endDateTime.isNotBlank()) {
                    InfoRowWithIcon(
                        icon = Icons.Default.Schedule,
                        label = "End Time",
                        value = formatDateTime(answer.endDateTime),
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )
                }

                answer.duration?.let { duration ->
                    InfoRowWithIcon(
                        icon = Icons.Default.Schedule,
                        label = "Duration",
                        value = "${duration} minutes",
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!answer.locationCoordinates.isNullOrBlank()) {
                    InfoRowWithIcon(
                        icon = Icons.Default.LocationOn,
                        label = "Coordinates",
                        value = answer.locationCoordinates,
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleDetailsCard(
    vehicle: app.forku.domain.model.vehicle.Vehicle,
    vehicleType: app.forku.domain.model.vehicle.VehicleType?,
    vehicleCategory: app.forku.domain.model.vehicle.VehicleCategory?,
    site: app.forku.domain.model.Site?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Vehicle Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider()

            InfoRowWithIcon(
                icon = Icons.Default.DirectionsCar,
                label = "Vehicle",
                value = vehicle.codename,
                valueColor = MaterialTheme.colorScheme.onSurface
            )

            if (vehicle.model.isNotBlank()) {
                InfoRowWithIcon(
                    icon = Icons.Default.Build,
                    label = "Model",
                    value = vehicle.model,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            vehicleType?.let { type ->
                InfoRowWithIcon(
                    icon = Icons.Default.Category,
                    label = "Vehicle Type",
                    value = type.Name,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            vehicleCategory?.let { category ->
                InfoRowWithIcon(
                    icon = Icons.Default.Label,
                    label = "Vehicle Category",
                    value = category.name,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            if (vehicle.serialNumber.isNotBlank()) {
                InfoRowWithIcon(
                    icon = Icons.Default.Tag,
                    label = "Serial Number",
                    value = vehicle.serialNumber,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            site?.let { siteInfo ->
                InfoRowWithIcon(
                    icon = Icons.Default.LocationOn,
                    label = "Location / Site",
                    value = siteInfo.name,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                
                if (siteInfo.address.isNotBlank()) {
                    InfoRowWithIcon(
                        icon = Icons.Default.LocationOn,
                        label = "Address",
                        value = siteInfo.address,
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (vehicle.description.isNotBlank()) {
                InfoRowWithIcon(
                    icon = Icons.Default.Info,
                    label = "Description",
                    value = vehicle.description,
                    valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChecklistInformationCard(checklist: app.forku.domain.model.checklist.Checklist) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Checklist Information",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider()

            InfoRowWithIcon(
                icon = Icons.Default.Assignment,
                label = "Title",
                value = checklist.title,
                valueColor = MaterialTheme.colorScheme.onSurface
            )

            if (checklist.description.isNotBlank()) {
                InfoRowWithIcon(
                    icon = Icons.Default.Info,
                    label = "Description",
                    value = checklist.description,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            InfoRowWithIcon(
                icon = Icons.Default.Assignment,
                label = "Total Questions",
                value = "${checklist.items.size}",
                valueColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AnsweredItemCard(
    answeredItem: AnsweredChecklistItem,
    multimedia: List<ChecklistItemAnswerMultimediaDto>
) {
    // Debug logging
    android.util.Log.d("CheckDetailScreen", "AnsweredItemCard: question='${answeredItem.question}', answer='${answeredItem.answer}', comment='${answeredItem.userComment}'")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (answeredItem.answer.toIntOrNull()) {
                0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) // PASS
                1 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) // FAIL
                2 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) // NA
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (answeredItem.question.isBlank()) {
                            "Question for Item: ${answeredItem.checklistItemId.take(8)}..."
                        } else {
                            answeredItem.question
                        },
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Show additional debug info
                    Text(
                        text = "Answer: ${answeredItem.answer} | Item ID: ${answeredItem.checklistItemId.take(8)}...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    
                    // User Comment Section
                    if (!answeredItem.userComment.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Comment,
                                    contentDescription = "Comment",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = answeredItem.userComment,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        // Show "No comment" for debugging purposes
                        Text(
                            text = "No comment provided",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                AnswerChip(answer = answeredItem.answer.toIntOrNull() ?: 0)
            }

            // Multimedia Section
            if (multimedia.isNotEmpty()) {
                MultimediaSection(multimedia = multimedia)
            }

            Text(
                text = "Answered: ${formatDateTime(answeredItem.createdAt)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MultimediaSection(multimedia: List<ChecklistItemAnswerMultimediaDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = "Media",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Attached Media (${multimedia.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Grid of images/videos
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(multimedia) { media ->
                    MultimediaItem(media = media)
                }
            }
        }
    }
}

@Composable
private fun MultimediaItem(media: ChecklistItemAnswerMultimediaDto) {
    val imageUrl = when {
        !media.imageUrl.isNullOrBlank() -> media.imageUrl
        !media.imageInternalName.isNullOrBlank() -> "${Constants.BASE_URL}api/multimedia/file/${media.id}/Image?t=%LASTEDITEDTIME%"
        else -> null
    }
    
    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable { 
                // TODO: Implement full-screen image view
                android.util.Log.d("MultimediaItem", "Image clicked: $imageUrl")
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (media.multimediaType) {
                1 -> { // Photo
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Attached photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onError = { error ->
                                android.util.Log.e("MultimediaItem", "Error loading image: $imageUrl", error.result.throwable)
                            }
                        )
                    } else {
                        // Fallback for missing image
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Photo,
                                    contentDescription = "Photo",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Photo",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                2 -> { // Video
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = "Video",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Video",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> { // Other media
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Media",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Media",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // File size overlay
            if (media.imageFileSize != null && media.imageFileSize > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "${(media.imageFileSize / 1024)}KB",
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRowWithIcon(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.weight(0.4f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            modifier = Modifier.weight(0.6f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun AnswerChip(answer: Int) {
    val (text, color, icon) = when (answer) {
        0 -> Triple("PASS", MaterialTheme.colorScheme.primary, Icons.Default.CheckCircle)
        1 -> Triple("FAIL", MaterialTheme.colorScheme.error, Icons.Default.Cancel)
        2 -> Triple("N/A", MaterialTheme.colorScheme.outline, Icons.Default.Info)
        else -> Triple("Unknown", MaterialTheme.colorScheme.outline, Icons.Default.Info)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

private fun formatDateTime(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString)
        date?.let { outputFormat.format(it) } ?: dateTimeString
    } catch (e: Exception) {
        dateTimeString
    }
}

// Keep existing components that are still used
@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

 