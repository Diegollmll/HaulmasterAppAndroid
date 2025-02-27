package app.forku.data.api.dto.session

data class StartSessionRequestDto(
    val vehicleId: String,
    val checkId: String,
    val timestamp: String,
    val startTime: String,
    val status: String,
    val userId: String,  // This should match the expected format from the API
    val endTime: String? = null
) 