package app.forku.presentation.dashboard

data class SystemOwnerDashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val feedbackSubmitted: Boolean = false,
    
    // System Overview
    val totalUsersCount: Int = 0,
    val totalVehiclesCount: Int = 0,
    val totalBusinessCount: Int = 0,
    val totalAdminsCount: Int = 0,
    val totalSuperAdminsCount: Int = 0,
    
    // System Health
    val systemHealth: SystemHealth = SystemHealth(),
    val apiLatency: Int = 0,
    val databaseSize: String = "0 MB",
    val activeConnections: Int = 0,
    
    // Business Overview
    val activeBusinesses: Int = 0,
    val pendingBusinesses: Int = 0,
    val suspendedBusinesses: Int = 0,
    val recentBusinesses: List<Business> = emptyList(),
    
    // User Management
    val recentUsers: List<app.forku.domain.model.user.User> = emptyList(),
    val pendingUserApprovals: Int = 0,
    val roleDistribution: Map<app.forku.domain.model.user.UserRole, Int> = emptyMap(),
    
    // System Settings
    val lastBackupTime: String? = null,
    val backupSize: String = "0 MB",
    val systemVersion: String = "1.0.0",
    val recentAuditLogs: List<AuditLogEntry> = emptyList()
) 