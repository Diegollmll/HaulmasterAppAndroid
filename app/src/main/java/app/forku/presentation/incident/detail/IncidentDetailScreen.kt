package app.forku.presentation.incident.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.utils.getRelativeTimeSpanFromMillis
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import app.forku.core.auth.TokenErrorHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun IncidentDetailScreen(
    incidentId: String,
    viewModel: IncidentDetailViewModel = hiltViewModel(),
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = { viewModel.loadIncidentDetail(incidentId) }
    )

    // Cargar los detalles cuando se inicia la pantalla
    LaunchedEffect(incidentId) {
        viewModel.loadIncidentDetail(incidentId)
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Incident Details",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
                    .padding(padding)
            ) {
                when {
                    state.isLoading -> LoadingOverlay()
                    state.error != null -> ErrorScreen(
                        message = state.error ?: "Unknown error occurred",
                        onRetry = { viewModel.loadIncidentDetail(incidentId) }
                    )
                    state.incident != null -> {
                        val incident = state.incident
                        Log.d("IncidentDetailScreen", "Incident object: $incident")
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(16.dp)
                        ) {
                            // Basic Information Card
                            DetailCard(title = "Basic Information") {
                                DetailRow("Type", incident?.type ?: "")
                                DetailRow("Status", incident?.status ?: "")
                                DetailRow("Severity", incident?.severityLevel ?: "")
                                DetailRow("Date", getRelativeTimeSpanFromMillis(incident?.date ?: 0L))
                                if (!incident?.weather.isNullOrBlank()) {
                                    DetailRow("Weather", incident?.weather ?: "")
                                }
                            }

                            // Location Information Card
                            DetailCard(title = "Location Information") {
                                DetailRow("Location", incident?.location ?: "")
                                if (incident?.locationDetails?.isNotEmpty() == true) {
                                    DetailRow("Details", incident.locationDetails)
                                }
                            }

                            // Vehicle Information Card
                            if (
                                !incident?.vehicleName.isNullOrBlank() ||
                                (!incident?.vehicleType.isNullOrBlank() && incident?.vehicleType != "Not specified") ||
                                !incident?.preshiftCheckStatus.isNullOrBlank() ||
                                (incident?.isLoadCarried == true && (!incident?.loadBeingCarried.isNullOrBlank() || !incident?.loadWeight.isNullOrBlank()))
                            ) {
                                DetailCard(title = "Vehicle Information") {
                                    if (!incident?.vehicleName.isNullOrBlank()) {
                                        DetailRow("Vehicle", incident?.vehicleName ?: "")
                                    }
                                    if (!incident?.vehicleType.isNullOrBlank() && incident?.vehicleType != "Not specified") {
                                        DetailRow("Type", incident?.vehicleType ?: "")
                                    }
                                    if (!incident?.preshiftCheckStatus.isNullOrBlank()) {
                                        DetailRow("Pre-shift Check Status", incident?.preshiftCheckStatus ?: "")
                                    }
                                    if (incident?.isLoadCarried == true) {
                                        if (!incident?.loadBeingCarried.isNullOrBlank()) {
                                            DetailRow("Load Being Carried", incident?.loadBeingCarried ?: "")
                                        }
                                        if (!incident?.loadWeight.isNullOrBlank()) {
                                            DetailRow("Load Weight", incident?.loadWeight ?: "")
                                        }
                                    }
                                }
                            }

                            // People Involved Card
                            if (incident?.othersInvolved?.isNotEmpty() == true || incident?.injuries?.isNotEmpty() == true) {
                                DetailCard(title = "People Involved") {
                                    if (incident?.othersInvolved?.isNotEmpty() == true) {
                                        DetailRow("Others Involved", incident.othersInvolved)
                                    }
                                    if (incident?.injuries?.isNotEmpty() == true) {
                                        DetailRow("Injuries", incident.injuries)
                                        if (incident?.injuryLocations?.isNotEmpty() == true) {
                                            DetailRow("Injury Locations", incident.injuryLocations.joinToString(", "))
                                        }
                                    }
                                }
                            }

                            // Description Card
                            DetailCard(title = "Description") {
                                    Text(
                                        text = incident?.description ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            // Type Specific Fields Card
                            incident?.typeSpecificFields?.let { fields ->
                                DetailCard(title = "Additional Details") {
                                    when (fields) {
                                        is app.forku.domain.model.incident.IncidentTypeFields.CollisionFields -> {
                                            if (fields.collisionType != null) {
                                                DetailRow("Collision Type", fields.collisionType.toFriendlyString())
                                            }
                                            if (fields.commonCause != null) {
                                                DetailRow("Common Cause", fields.commonCause.toFriendlyString())
                                            }
                                            if (fields.contributingFactors.isNotEmpty()) {
                                                DetailRow("Contributing Factors", fields.contributingFactors.joinToString(", ") { it.toFriendlyString() })
                                            }
                                            if (fields.damageOccurrence.isNotEmpty()) {
                                                DetailRow("Damage Occurrence", fields.damageOccurrence.joinToString(", ") { it.toFriendlyString() })
                                            }
                                            if (fields.environmentalImpact != null && fields.environmentalImpact.isNotEmpty()) {
                                                DetailRow("Environmental Impact", fields.environmentalImpact.joinToString(", ") { v -> v.toFriendlyString() })
                                            }
                                            if (fields.immediateActions.isNotEmpty()) {
                                                DetailRow("Immediate Actions", fields.immediateActions.joinToString(", ") { it.toFriendlyString() })
                                            }
                                            if (fields.immediateCause != null) {
                                                DetailRow("Immediate Cause", fields.immediateCause.toFriendlyString())
                                            }
                                            if (fields.injurySeverity != null) {
                                                DetailRow("Injury Severity", fields.injurySeverity.toFriendlyString())
                                            }
                                            if (fields.injuryLocations.isNotEmpty()) {
                                                DetailRow("Injury Locations", fields.injuryLocations.joinToString(", "))
                                            }
                                            if (fields.longTermSolutions.isNotEmpty()) {
                                                DetailRow("Long Term Solutions", fields.longTermSolutions.joinToString(", ") { it.toFriendlyString() })
                                            }
                                        }
                                        is app.forku.domain.model.incident.IncidentTypeFields.VehicleFailFields -> {
                                            if (fields.failureType != null) {
                                                DetailRow("Failure Type", fields.failureType.toFriendlyString())
                                            }
                                            if (fields.damageOccurrence.isNotEmpty()) {
                                                DetailRow("Damage Occurrence", fields.damageOccurrence.joinToString(", ") { it.toFriendlyString() })
                                            }
                                            if (fields.immediateCause != null) {
                                                DetailRow("Immediate Cause", fields.immediateCause.toFriendlyString())
                                            }
                                            if (fields.contributingFactors.isNotEmpty()) {
                                                DetailRow("Contributing Factors", fields.contributingFactors.joinToString(", ") { it.toFriendlyString() })
                                            }
                                            if (fields.environmentalImpact != null && fields.environmentalImpact.isNotEmpty()) {
                                                DetailRow("Environmental Impact", fields.environmentalImpact.joinToString(", ") { v -> v.toFriendlyString() })
                                            }
                                            if (fields.immediateActions.isNotEmpty()) {
                                                DetailRow("Immediate Actions", fields.immediateActions.joinToString(", ") { it.toFriendlyString() })
                                            }
                                            if (fields.longTermSolutions.isNotEmpty()) {
                                                DetailRow("Long Term Solutions", fields.longTermSolutions.joinToString(", ") { it.toFriendlyString() })
                                            }
                                        }
                                        is app.forku.domain.model.incident.IncidentTypeFields.HazardFields -> {
                                            if (fields.hazardType != null) {
                                                DetailRow("Hazard Type", fields.hazardType.toFriendlyString())
                                            }
                                            if (fields.potentialConsequences.isNotEmpty()) {
                                                DetailRow("Potential Consequences", fields.potentialConsequences.joinToString(", ") { it.toFriendlyString() })
                                            }
                                            if (fields.correctiveActions.isNotEmpty()) {
                                                DetailRow("Corrective Actions", fields.correctiveActions.joinToString(", ") { it.toFriendlyString() })
                                            }
                                            if (fields.preventiveMeasures.isNotEmpty()) {
                                                DetailRow("Preventive Measures", fields.preventiveMeasures.joinToString(", ") { it.toFriendlyString() })
                                            }
                                        }
                                        is app.forku.domain.model.incident.IncidentTypeFields.NearMissFields -> {
                                            if (fields.nearMissType != null) {
                                                DetailRow("Near Miss Type", fields.nearMissType.toFriendlyString())
                                            }
                                            if (fields.immediateCause != null) {
                                                DetailRow("Immediate Cause", fields.immediateCause.toFriendlyString())
                                            }
                                            if (fields.contributingFactors.isNotEmpty()) {
                                                DetailRow("Contributing Factors", fields.contributingFactors.joinToString(", ") { it.toFriendlyString() })
                                            }
                                            if (fields.immediateActions.isNotEmpty()) {
                                                DetailRow("Immediate Actions", fields.immediateActions.joinToString(", ") { it.toFriendlyString() })
                                            }
                                            if (fields.longTermSolutions.isNotEmpty()) {
                                                DetailRow("Long Term Solutions", fields.longTermSolutions.joinToString(", ") { it.toFriendlyString() })
                                            }
                                        }
                                    }
                                }
                            }

                            // Attachments Card
                                    if (incident?.attachments?.isNotEmpty() == true) {
                                DetailCard(title = "Attachments") {
                                    incident.attachments.forEach { attachment ->
                                                Text(
                                                    text = attachment,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
    )
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
} 