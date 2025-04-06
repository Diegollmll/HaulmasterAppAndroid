package app.forku.presentation.dashboard

data class Business(
    val id: String,
    val name: String,
    val totalUsers: Int,
    val totalVehicles: Int,
    val status: BusinessStatus,
    val systemOwnerId: String? = null,
    val superAdminId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
