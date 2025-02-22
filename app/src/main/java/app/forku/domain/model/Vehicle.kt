package app.forku.domain.model

import VehicleStatus

data class Vehicle(
    val id: String,
    val type: VehicleType,
    val status: VehicleStatus,
    val serialNumber: String,
    val qrCode: String,
    val lastCheck: VehicleCheck
)

data class VehicleCheck(
    val timestamp: String,
    val status: String
)