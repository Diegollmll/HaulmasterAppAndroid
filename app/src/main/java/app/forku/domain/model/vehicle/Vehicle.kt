package app.forku.domain.model.vehicle

import app.forku.domain.model.checklist.PreShiftCheck
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Vehicle(
    val id: String,
    val codename: String,
    val type: VehicleType,
    val status: VehicleStatus,
    val manufacturer: String,
    val model: String,
    val serialNumber: String,
    val year: Int,
    val lastMaintenanceDate: String? = null,
    val description: String,
    val bestSuitedFor: String,
    val photoModel: String,
    val vehicleClass: String,
    val energyType: String,
    val nextService: String,
    val hasIssues: Boolean = false,
    val maintenanceStatus: MaintenanceStatus = MaintenanceStatus.UP_TO_DATE
) {
    val needsMaintenance: Boolean
        get() = maintenanceStatus != MaintenanceStatus.UP_TO_DATE
}

enum class MaintenanceStatus {
    UP_TO_DATE,
    DUE_SOON,
    OVERDUE
}