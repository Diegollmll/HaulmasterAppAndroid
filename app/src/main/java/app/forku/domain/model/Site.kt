package app.forku.domain.model

data class Site(
    val id: String,
    val name: String,
    val address: String = "",
    val businessId: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
) 