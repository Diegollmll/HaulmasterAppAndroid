package app.forku.data.model

import com.google.gson.annotations.SerializedName

data class VehicleSessionDto(
    val id: String,
    val vehicleId: String,
    val userId: String,
    val checkId: String,
    val startTime: String,
    val endTime: String?,
    val timestamp: String,
    val status: String,
    val startLocationCoordinates: String?,
    val endLocationCoordinates: String?,
    val closeMethod: String?,
    val closedBy: String?,
    val notes: String?
) 