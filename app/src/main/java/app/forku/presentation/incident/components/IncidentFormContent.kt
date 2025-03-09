package app.forku.presentation.incident.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.incident.IncidentReportState
import app.forku.presentation.common.components.ExpandableCard
import app.forku.presentation.common.components.ContentCard
import androidx.compose.ui.graphics.Color
import app.forku.presentation.common.components.ForkuButton

@Composable
fun IncidentFormContent(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Basic Incident Details Section (Always visible)
        ExpandableCard(
            title = "Incident Details",
            initiallyExpanded = true,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp
            )
        ) {
            ContentCard(
                backgroundColor = Color.White,
                modifier = Modifier.padding(0.dp)
            ) {
                IncidentDetailsSection(state = state, onValueChange = onValueChange)
            }
        }

        if (state.type == IncidentType.HAZARD) {
            ExpandableCard(
                title = "Immediate Actions Taken",
                initiallyExpanded = true,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp
                )
            ) {
                ContentCard(
                    backgroundColor = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    HazardImmediateActionsSection(
                        state = state,
                        onValueChange = onValueChange,
                        modifier = Modifier.padding(horizontal = 13.dp)
                    )
                }
            }
        }

        if (state.type in listOf(IncidentType.COLLISION, IncidentType.NEAR_MISS)) {
            // People Involved Section
            ExpandableCard(
                title = "People Involved",
                initiallyExpanded = true,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp
                )
            ) {
                ContentCard(
                    backgroundColor = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    PeopleInvolvedSection(state = state, onValueChange = onValueChange)
                }
            }
        }

        // Vehicle Info Section (Only for specific types)
        if (state.type in listOf(IncidentType.COLLISION, IncidentType.VEHICLE_FAIL, IncidentType.NEAR_MISS)) {
            ExpandableCard(
                title = "Vehicle Info",
                initiallyExpanded = true,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp
                )
            ) {
                ContentCard(
                    backgroundColor = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    VehicleInfoSection(state = state, onValueChange = onValueChange)
                }
            }
        }

        // Incident Description Section with Photos
        ExpandableCard(
            title = "Incident Description",
            initiallyExpanded = true,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp
            )
        ) {
            ContentCard(
                backgroundColor = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                IncidentDescriptionSection(
                    state = state,
                    onValueChange = onValueChange,
                    onAddPhoto = onAddPhoto
                )
            }
        }

        if (state.type in listOf(IncidentType.COLLISION, IncidentType.VEHICLE_FAIL, IncidentType.NEAR_MISS)) {
            ExpandableCard(
                title = "Root Cause Analysis",
                initiallyExpanded = true,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp
                )
            ) {
                ContentCard(
                    backgroundColor = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    RootCauseAnalysisSection(state = state, onValueChange = onValueChange)
                }
            }
        }

        if (state.type in listOf(IncidentType.COLLISION, IncidentType.VEHICLE_FAIL)) {
            ExpandableCard(
                title = "Damage & Impact",
                initiallyExpanded = true,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp
                )
            ) {
                ContentCard(
                    backgroundColor = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    DamageAndImpactSection(state = state, onValueChange = onValueChange)
                }
            }
        }

        if (state.type in listOf(IncidentType.COLLISION, IncidentType.VEHICLE_FAIL, IncidentType.NEAR_MISS)) {
            ExpandableCard(
                title = "Potential Solutions",
                initiallyExpanded = true,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp
                )
            ) {
                ContentCard(
                    backgroundColor = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    PotentialSolutionsSection(
                        state = state,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Add Hazard Preventive Measures section only for HAZARD type
        if (state.type == IncidentType.HAZARD) {
            ExpandableCard(
                title = "Preventive Measures",
                initiallyExpanded = true,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp
                )
            ) {
                ContentCard(
                    backgroundColor = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    HazardPreventiveMeasuresSection(
                        state = state,
                        onValueChange = onValueChange,
                        modifier = Modifier.padding(horizontal = 13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}