package app.forku.data.api.dto

data class VehicleDto(
    val id: String,
    val type: VehicleTypeDto,
    val status: String,
    val serialNumber: String,
    val qrCode: String,
    val lastCheck: VehicleCheckDto
)

data class VehicleCheckDto(
    val timestamp: String,
    val status: String
)

data class VehicleTypeDto(
    val id: String,
    val name: String,
    val requiresCertification: Boolean
)