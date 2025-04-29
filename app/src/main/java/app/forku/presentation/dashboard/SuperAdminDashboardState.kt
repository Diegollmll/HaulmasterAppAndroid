package app.forku.presentation.dashboard

import app.forku.domain.model.business.BusinessStatus

data class SuperAdminDashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val feedbackSubmitted: Boolean = false,
    
    // System Overview
    val totalUsersCount: Int = 0,
    val totalVehiclesCount: Int = 0,
    val activeAdminsCount: Int = 0,
    val totalBusinessCount: Int = 0,
    
    // User Management
    val recentUsers: List<app.forku.domain.model.user.User> = emptyList(),
    val pendingUserApprovals: Int = 0,
    
    // Business Management
    val recentBusinesses: List<Business> = emptyList(),
    val pendingBusinessApprovals: Int = 0,
    val unassignedUsers: Int = 0,
    
    // Vehicle Management
    val maintenanceAlerts: Int = 0,
    val vehicleIssues: Int = 0,
    
    // System Settings
    val systemHealth: SystemHealth = SystemHealth(),
    val lastBackupTime: String? = null,
    val recentAuditLogs: List<AuditLogEntry> = emptyList(),
    val businesses: List<Business> = emptyList(),
    val totalBusinesses: Int = 0,
    val totalUsers: Int = 0,
    val businessesByStatus: Map<BusinessStatus, Int> = emptyMap()
)

data class SystemHealth(
    val serverStatus: String = "OK",
    val databaseStatus: String = "OK",
    val apiStatus: String = "OK",
    val lastCheckTime: String? = null
)

data class AuditLogEntry(
    val id: String,
    val action: String,
    val user: String,
    val timestamp: String,
    val details: String
) 