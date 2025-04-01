package app.forku.data.api.dto.session

data class StartSessionRequestDto(
    val vehicleId: String,
    val checkId: String,
    val userId: String,
    val startTime: String,
    val timestamp: String,
    val status: String,
    val startLocation: String? = null,
    val startLocationCoordinates: String? = null
) 