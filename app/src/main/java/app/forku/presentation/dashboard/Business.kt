package app.forku.presentation.dashboard

import app.forku.domain.model.business.BusinessStatus

data class Business(
    val id: String,
    val name: String,
    val totalUsers: Int = 0,
    val totalVehicles: Int = 0,
    val status: BusinessStatus,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    val settings: Map<String, String> = emptyMap(),
    val metadata: Map<String, String> = emptyMap(),
    val superAdminId: String? = null
)
