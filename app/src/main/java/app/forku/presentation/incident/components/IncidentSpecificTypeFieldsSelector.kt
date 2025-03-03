package app.forku.presentation.incident.components

import androidx.compose.runtime.Composable
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.incident.IncidentReportState


@Composable
fun IncidentSpecificTypeFieldsSelector(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit
) {
    when (state.type) {
        IncidentType.COLLISION -> {
            CollisionTypeSpecificField(state = state, onValueChange = onValueChange)
        }
        IncidentType.HAZARD -> {
            HazardTypeSpecificField(state = state, onValueChange = onValueChange)
        }
        IncidentType.NEAR_MISS -> {
            NearMissTypeSpecificField(state = state, onValueChange = onValueChange)
        }
        IncidentType.VEHICLE_FAIL -> {
            VehicleFailSpecificField(state = state, onValueChange = onValueChange)
        }
        else -> null
    }
} 