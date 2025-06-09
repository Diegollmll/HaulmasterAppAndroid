package app.forku.data.api.dto.session

import app.forku.data.api.dto.user.UserDto
import app.forku.data.api.dto.vehicle.VehicleDto

data class VehicleSessionDto(
    val Id: String,
    val ChecklistAnswerId: String,
    val GOUserId: String,
    val VehicleId: String,
    val StartTime: String,
    val EndTime: String? = null,
    val Status: Int,
    val StartLocationCoordinates: String? = null,
    val EndLocationCoordinates: String? = null,
    val Timestamp: String,
    val VehicleSessionClosedMethod: String? = null,
    val ClosedBy: String? = null,
    val IsDirty: Boolean = true,
    val IsNew: Boolean = true,
    val IsMarkedForDeletion: Boolean = false,
    val Duration: Int? = null,
    val GOUser: UserDto? = null,
    val Vehicle: VehicleDto? = null
)