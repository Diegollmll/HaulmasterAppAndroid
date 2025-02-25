package app.forku.data.api.dto.checklist

import app.forku.domain.model.checklist.Answer

data class PerformChecklistRequestDto(
    val items: List<PerformChecklistItemRequestDto>,
    val datetime: String = java.time.Instant.now().toString(),
    val status: String,
    val userId: String
)