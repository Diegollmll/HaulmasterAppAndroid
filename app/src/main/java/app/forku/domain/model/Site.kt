package app.forku.domain.model

data class Site(
    val id: String,
    val name: String,
    val address: String,
    val businessId: String,
    val latitude: Double,
    val longitude: Double,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
) 