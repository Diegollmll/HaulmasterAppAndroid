package app.forku.domain.model.feedback

data class Feedback(
    val id: String? = null,
    val userId: String,
    val rating: Int,
    val comment: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
) 