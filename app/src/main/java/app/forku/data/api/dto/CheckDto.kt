package app.forku.data.api.dto

data class CheckRequestDto(
    val items: List<CheckItemRequestDto>
)

data class CheckItemRequestDto(
    val id: String,
    val expectedAnswer: Boolean
)

data class CheckResponseDto(
    val id: String,
    val timestamp: String,
    val status: String
) 