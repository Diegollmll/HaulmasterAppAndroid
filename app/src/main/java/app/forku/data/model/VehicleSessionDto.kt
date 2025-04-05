package app.forku.data.model

import com.google.gson.annotations.SerializedName

data class VehicleSessionDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("vehicle_id")
    val vehicleId: String,
    @SerializedName("operator_id")
    val userId: String,
    @SerializedName("check_id")
    val checkId: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String?,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("start_location_coordinates")
    val startLocationCoordinates: String?,
    @SerializedName("end_location_coordinates")
    val endLocationCoordinates: String?,
    @SerializedName("close_method")
    val closeMethod: String?,
    @SerializedName("closed_by")
    val closedBy: String?,
    @SerializedName("notes")
    val notes: String?
) 