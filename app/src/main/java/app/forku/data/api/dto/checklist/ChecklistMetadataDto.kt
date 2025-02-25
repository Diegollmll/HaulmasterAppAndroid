package app.forku.data.api.dto.checklist

data class ChecklistMetadataDto(
    val version: String,
    val lastUpdated: String,
    val totalQuestions: Int,
    val rotationGroups: Int,
    val questionsPerCheck: Int,
    val energySources: List<String>
) 