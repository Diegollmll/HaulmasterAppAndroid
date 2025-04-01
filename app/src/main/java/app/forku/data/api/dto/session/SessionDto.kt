package app.forku.data.api.dto.session

import androidx.datastore.preferences.protobuf.Timestamp

data class SessionDto(
    val id: String,
    val vehicleId: String,
    val userId: String,
    val startTime: String,
    val endTime: String?,
    val status: String,
    val startLocationCoordinates: String?,
    val endLocationCoordinates: String?,
    val timestamp: String,
    val closeMethod: String? = null,
    val closedBy: String? = null,
    val notes: String? = null
)