package app.forku.domain.model.user

data class UserSite(
    val id: String,
    val siteId: String,
    val goUserId: String,
    val isDirty: Boolean = false
) 