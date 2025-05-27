package app.forku.presentation.incident.components

import androidx.compose.runtime.Composable
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.presentation.incident.IncidentReportState


@Composable
fun IncidentSpecificTypeFieldsSelector(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit
) {
    when (state.type) {
        IncidentTypeEnum.COLLISION -> {
            CollisionTypeSpecificField(state = state, onValueChange = onValueChange)
        }
        IncidentTypeEnum.HAZARD -> {
            HazardTypeSpecificField(state = state, onValueChange = onValueChange)
        }
        IncidentTypeEnum.NEAR_MISS -> {
            NearMissTypeSpecificField(state = state, onValueChange = onValueChange)
        }
        IncidentTypeEnum.VEHICLE_FAIL -> {
            VehicleFailSpecificField(state = state, onValueChange = onValueChange)
        }
        else -> null
    }
} 