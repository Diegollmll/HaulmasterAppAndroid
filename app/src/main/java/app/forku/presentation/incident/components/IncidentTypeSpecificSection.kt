package app.forku.presentation.incident.components

import androidx.compose.runtime.Composable
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.common.components.ExpandableCard
import app.forku.presentation.incident.IncidentReportState
import app.forku.presentation.incident.components.CollisionSpecificFields
import app.forku.presentation.incident.components.HazardSpecificFields
import app.forku.presentation.incident.components.NearMissSpecificFields
import app.forku.presentation.incident.components.VehicleFailureSpecificFields


@Composable
fun IncidentTypeSpecificSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit
) {
    when (state.type) {
        IncidentType.COLLISION -> {
            ExpandableCard(title = "Collision Details") {
                CollisionSpecificFields(state = state, onValueChange = onValueChange)
            }
        }
        IncidentType.HAZARD -> {
            ExpandableCard(title = "Hazard Details") {
                HazardSpecificFields(state = state, onValueChange = onValueChange)
            }
        }
        IncidentType.NEAR_MISS -> {
            ExpandableCard(title = "Near Miss Details") {
                NearMissSpecificFields(state = state, onValueChange = onValueChange)
            }
        }
        IncidentType.VEHICLE_FAIL -> {
            ExpandableCard(title = "Vehicle Failure Details") {
                VehicleFailureSpecificFields(state = state, onValueChange = onValueChange)
            }
        }
        else -> null
    }
} 