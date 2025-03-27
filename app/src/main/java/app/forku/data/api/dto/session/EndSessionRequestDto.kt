package app.forku.data.api.dto.session

data class EndSessionRequestDto(
    val timestamp: String,
    val endTime: String,
    val status: String,
    val notes: String?,
    val closeMethod: String,
    val closedBy: String? = null
)