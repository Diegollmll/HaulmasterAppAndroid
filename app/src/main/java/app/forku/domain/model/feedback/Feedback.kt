package app.forku.domain.model.feedback

data class Feedback(
    val id: String? = null,
    val canContactMe: Boolean = false,
    val comment: String,
    val goUserId: String,
    val rating: Int
) 