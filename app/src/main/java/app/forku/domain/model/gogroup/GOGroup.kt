package app.forku.domain.model.gogroup

data class GOGroup(
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?
) 