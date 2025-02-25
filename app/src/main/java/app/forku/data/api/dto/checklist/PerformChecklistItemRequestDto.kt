package app.forku.data.api.dto.checklist

data class PerformChecklistItemRequestDto(
    val id: String,
    val expectedAnswer: String,
    val userAnswer: String
) 