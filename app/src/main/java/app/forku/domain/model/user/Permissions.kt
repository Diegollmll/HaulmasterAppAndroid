package app.forku.domain.model.user

object Permissions {
    // Admin Permissions
    const val MANAGE_USERS = "MANAGE_USERS"
    const val MANAGE_VEHICLES = "MANAGE_VEHICLES"
    const val VIEW_ALL_REPORTS = "VIEW_ALL_REPORTS"
    const val MANAGE_CERTIFICATIONS = "MANAGE_CERTIFICATIONS"
    const val MANAGE_TRAINING = "MANAGE_TRAINING"
    
    // Operator Permissions
    const val OPERATE_VEHICLE = "OPERATE_VEHICLE"
    const val REPORT_INCIDENT = "REPORT_INCIDENT"
    const val VIEW_OWN_REPORTS = "VIEW_OWN_REPORTS"
    const val TAKE_TRAINING = "TAKE_TRAINING"
    
    // Default permission sets by role
    val ADMIN_PERMISSIONS = setOf(
        MANAGE_USERS,
        MANAGE_VEHICLES,
        VIEW_ALL_REPORTS,
        MANAGE_CERTIFICATIONS,
        MANAGE_TRAINING
    )
    
    val OPERATOR_PERMISSIONS = setOf(
        OPERATE_VEHICLE,
        REPORT_INCIDENT,
        VIEW_OWN_REPORTS,
        TAKE_TRAINING
    )
    
    val USER_PERMISSIONS = setOf(
        TAKE_TRAINING
    )
}

// Extension function to check if a user has a specific permission
fun User.hasPermission(permission: String): Boolean {
    return permissions.contains(permission)
}

// Extension function to check if a user has all required permissions
fun User.hasAllPermissions(requiredPermissions: Set<String>): Boolean {
    return permissions.containsAll(requiredPermissions)
}

// Extension function to check if a user has any of the specified permissions
fun User.hasAnyPermission(permissions: Set<String>): Boolean {
    return this.permissions.any { it in permissions }
} 