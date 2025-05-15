package app.forku.domain.model.vehicle

import app.forku.domain.model.checklist.PreShiftCheck
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Vehicle(
    val id: String,
    val codename: String,
    val type: VehicleType,
    val status: VehicleStatus,
    val model: String,
    val serialNumber: String,
    val description: String,
    val bestSuitedFor: String,
    val photoModel: String,
    val categoryId: String,
    val energyType: String,
    val nextService: String,
    val hasIssues: Boolean = false,
    val businessId: String? = null
) {

    fun getPictureUrl(baseUrl: String, lastEditedTime: String? = "%LASTEDITEDTIME%"): String {
        val timeParam = lastEditedTime?.let { "?t=$it" } ?: ""
        val url = "$baseUrl" + "api/vehicle/file/$id/Picture$timeParam"
        android.util.Log.d("VehicleImage", "Generated vehicle image URL: $url")
        return url
    }
}

enum class MaintenanceStatus {
    UP_TO_DATE,
    DUE_SOON,
    OVERDUE
}

