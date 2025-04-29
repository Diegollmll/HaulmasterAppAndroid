package app.forku.domain.model.gogroup

data class GOGroupRole(
    val groupName: String,
    val roleName: String,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?
) 