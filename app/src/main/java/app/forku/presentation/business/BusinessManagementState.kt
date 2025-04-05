package app.forku.presentation.business

import app.forku.presentation.dashboard.Business

data class BusinessManagementState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val businesses: List<Business> = emptyList(),
    val totalBusinesses: Int = 0,
    val pendingApprovals: Int = 0,
    val unassignedUsers: Int = 0,
    val showAddBusinessDialog: Boolean = false
) 