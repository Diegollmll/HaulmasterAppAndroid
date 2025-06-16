package app.forku.core.auth

import android.util.Log
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.data.api.dto.gouserrole.GOUserRoleDto

/**
 * Centralized manager for ALL user roles and permissions logic
 * This is the single source of truth for role-related operations
 */
object UserRoleManager {
    
    private const val TAG = "UserRoleManager"
    
    // ========================================
    // ROLE DETERMINATION & CONVERSION
    // ========================================
    
    /**
     * Determines the effective role for a user in a specific business context
     * Priority: UserRoleItems (general system roles) > Business assignments
     * 
     * @param user The user object with general role
     * @param userRoleItems Raw role items from API (if available)
     * @param businessRole Business-specific role assignment (usually OPERATOR)
     * @param businessId Current business context
     * @return The effective UserRole for the user
     */
    fun getEffectiveRole(
        user: User? = null,
        userRoleItems: List<GOUserRoleDto>? = null,
        businessRole: UserRole? = null,
        businessId: String? = null
    ): UserRole {
        Log.d(TAG, "=== Determining Effective Role ===")
        Log.d(TAG, "User: ${user?.fullName} (${user?.id})")
        Log.d(TAG, "User general role: ${user?.role}")
        Log.d(TAG, "UserRoleItems count: ${userRoleItems?.size ?: 0}")
        Log.d(TAG, "Business role: $businessRole")
        Log.d(TAG, "Business context: $businessId")
        
        // 1. Priority: Use UserRoleItems if available (most accurate)
        if (!userRoleItems.isNullOrEmpty()) {
            val effectiveRole = determineRoleFromUserRoleItems(userRoleItems)
            Log.d(TAG, "✅ Using UserRoleItems role: $effectiveRole")
            return effectiveRole
        }
        
        // 2. Fallback: Use user's general role
        if (user?.role != null && user.role != UserRole.OPERATOR) {
            Log.d(TAG, "✅ Using user general role: ${user.role}")
            return user.role
        }
        
        // 3. Last resort: Use business role or default to OPERATOR
        val finalRole = businessRole ?: UserRole.OPERATOR
        Log.d(TAG, "✅ Using fallback role: $finalRole")
        return finalRole
    }
    
    /**
     * Converts a string role to UserRole enum
     * @param roleString The role string to convert
     * @param defaultRole The default role to return if conversion fails (defaults to OPERATOR)
     * @return The corresponding UserRole
     */
    fun fromString(roleString: String?, defaultRole: UserRole = UserRole.OPERATOR): UserRole {
        Log.d(TAG, "=== Converting role string ===")
        Log.d(TAG, "Input roleString: '$roleString'")
        Log.d(TAG, "Default role: $defaultRole")
        
        if (roleString == null) {
            Log.d(TAG, "Role string is null, returning default: $defaultRole")
            return defaultRole
        }
        
        val normalized = roleString.trim().lowercase().replace(" ", "")
        Log.d(TAG, "Normalized role string: '$normalized'")
        
        return try {
            val result = when (normalized) {
                "systemowner", "system_owner" -> UserRole.SYSTEM_OWNER
                "superadmin", "super_admin" -> UserRole.SUPERADMIN
                "admin", "administrator", "administrador" -> UserRole.ADMIN
                "operator", "operador", "user" -> UserRole.OPERATOR
                else -> {
                    Log.d(TAG, "No direct match, trying enum parse...")
                    // Try to parse as enum name directly
                    try {
                        val enumResult = UserRole.valueOf(roleString.uppercase())
                        Log.d(TAG, "Enum parse successful: $enumResult")
                        enumResult
                    } catch (e: IllegalArgumentException) {
                        Log.w(TAG, "Could not convert role string: '$roleString', using default role: $defaultRole")
                        defaultRole
                    }
                }
            }
            Log.d(TAG, "Final conversion result: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error converting role string: '$roleString'", e)
            defaultRole
        }
    }
    
    /**
     * Converts a UserRole to its corresponding API string representation
     * @param role The UserRole to convert
     * @return The string representation for the API
     */
    fun toApiString(role: UserRole): String {
        return when (role) {
            UserRole.SYSTEM_OWNER -> "systemowner"
            UserRole.SUPERADMIN -> "superadmin"
            UserRole.ADMIN -> "administrator"
            UserRole.OPERATOR -> "operator"
        }
    }

    /**
     * Converts a UserRole to a user-friendly display string
     * @param role The UserRole to convert
     * @return The user-friendly string for UI display
     */
    fun toDisplayString(role: UserRole): String {
        return when (role) {
            UserRole.SYSTEM_OWNER -> "System Owner"
            UserRole.SUPERADMIN -> "Super Admin"
            UserRole.ADMIN -> "Administrator"
            UserRole.OPERATOR -> "Operator"
        }
    }
    
    /**
     * Determines role from UserRoleItems with proper priority handling
     */
    private fun determineRoleFromUserRoleItems(userRoleItems: List<GOUserRoleDto>): UserRole {
        Log.d(TAG, "--- Analyzing UserRoleItems ---")
        
        userRoleItems.forEachIndexed { index, roleItem ->
            Log.d(TAG, "Role $index: ${roleItem.gORoleName}, active: ${roleItem.isActive}")
        }
        
        // 1. Look for active role first
        val activeRole = userRoleItems.find { it.isActive }
        if (activeRole != null) {
            val convertedRole = fromString(activeRole.gORoleName)
            Log.d(TAG, "Found active role: ${activeRole.gORoleName} -> $convertedRole")
            return convertedRole
        }
        
        // 2. Priority order: Admin > SuperAdmin > SystemOwner > User/Operator
        val priorityOrder = listOf("Administrator", "SuperAdmin", "SystemOwner", "User")
        
        for (priority in priorityOrder) {
            val roleItem = userRoleItems.find { 
                it.gORoleName.equals(priority, ignoreCase = true) 
            }
            if (roleItem != null) {
                val convertedRole = fromString(roleItem.gORoleName)
                Log.d(TAG, "Found priority role: ${roleItem.gORoleName} -> $convertedRole")
                return convertedRole
            }
        }
        
        // 3. Fallback to first role
        val firstRole = userRoleItems.firstOrNull()
        if (firstRole != null) {
            val convertedRole = fromString(firstRole.gORoleName)
            Log.d(TAG, "Using first role: ${firstRole.gORoleName} -> $convertedRole")
            return convertedRole
        }
        
        Log.d(TAG, "No roles found, defaulting to OPERATOR")
        return UserRole.OPERATOR
    }
    
    // ========================================
    // PERMISSIONS & ACCESS CONTROL
    // ========================================
    
    /**
     * Checks if a user has a specific permission in the current context
     */
    fun hasPermission(
        userRole: UserRole,
        permission: Permission,
        businessContext: String? = null
    ): Boolean {
        return when (permission) {
            Permission.VIEW_ADMIN_DASHBOARD -> isAdminUser(userRole)
            Permission.MANAGE_USERS -> isAdminUser(userRole)
            Permission.MANAGE_VEHICLES -> isAdminUser(userRole)
            Permission.VIEW_REPORTS -> isAdminUser(userRole)
            Permission.MANAGE_BUSINESS -> userRole in listOf(UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER)
            Permission.SYSTEM_ADMINISTRATION -> userRole == UserRole.SYSTEM_OWNER
            Permission.OPERATE_VEHICLE -> true // All users can operate vehicles
            Permission.VIEW_INCIDENTS -> true // All users can view incidents
            Permission.CREATE_INCIDENTS -> true // All users can create incidents
            Permission.VIEW_CHECKLISTS -> true // All users can view checklists
            Permission.CREATE_CHECKLISTS -> isAdminUser(userRole)
        }
    }
    
    /**
     * Gets all permissions for a given role
     */
    fun getPermissionsForRole(role: UserRole): Set<Permission> {
        return Permission.values().filter { permission ->
            hasPermission(role, permission)
        }.toSet()
    }
    
    /**
     * Determines if user should see admin features
     */
    fun isAdminUser(role: UserRole): Boolean {
        return role in listOf(UserRole.ADMIN, UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER)
    }
    
    /**
     * Determines if user is a super user (SuperAdmin or SystemOwner)
     */
    fun isSuperUser(role: UserRole): Boolean {
        return role in listOf(UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER)
    }
    
    /**
     * Determines if user can manage multiple businesses
     */
    fun canManageMultipleBusinesses(role: UserRole): Boolean {
        return role in listOf(UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER)
    }
    
    // ========================================
    // NAVIGATION & ROUTING
    // ========================================
    
    /**
     * Gets the appropriate dashboard route for a user role
     */
    fun getDashboardRoute(role: UserRole): String {
        return when (role) {
            UserRole.SYSTEM_OWNER -> app.forku.presentation.navigation.Screen.SystemOwnerDashboard.route
            UserRole.SUPERADMIN -> app.forku.presentation.navigation.Screen.SuperAdminDashboard.route
            UserRole.ADMIN -> app.forku.presentation.navigation.Screen.AdminDashboard.route
            UserRole.OPERATOR -> app.forku.presentation.navigation.Screen.Dashboard.route
        }
    }
    
    /**
     * Determines if user should see specific navigation items
     */
    fun shouldShowNavigationItem(role: UserRole, navigationItem: NavigationItem): Boolean {
        return when (navigationItem) {
            NavigationItem.ADMIN_DASHBOARD -> isAdminUser(role)
            NavigationItem.USER_MANAGEMENT -> hasPermission(role, Permission.MANAGE_USERS)
            NavigationItem.VEHICLE_MANAGEMENT -> hasPermission(role, Permission.MANAGE_VEHICLES)
            NavigationItem.REPORTS -> hasPermission(role, Permission.VIEW_REPORTS)
            NavigationItem.BUSINESS_MANAGEMENT -> hasPermission(role, Permission.MANAGE_BUSINESS)
            NavigationItem.SYSTEM_SETTINGS -> hasPermission(role, Permission.SYSTEM_ADMINISTRATION)
            NavigationItem.OPERATOR_DASHBOARD -> true
            NavigationItem.VEHICLES -> true
            NavigationItem.INCIDENTS -> true
            NavigationItem.CHECKLISTS -> true
        }
    }
    
    // ========================================
    // ROLE HIERARCHY & COMPARISON
    // ========================================
    
    /**
     * Gets the hierarchy level of a role (higher number = more permissions)
     */
    fun getRoleHierarchyLevel(role: UserRole): Int {
        return when (role) {
            UserRole.OPERATOR -> 1
            UserRole.ADMIN -> 2
            UserRole.SUPERADMIN -> 3
            UserRole.SYSTEM_OWNER -> 4
        }
    }
    
    /**
     * Checks if roleA has higher or equal permissions than roleB
     */
    fun hasHigherOrEqualPermissions(roleA: UserRole, roleB: UserRole): Boolean {
        return getRoleHierarchyLevel(roleA) >= getRoleHierarchyLevel(roleB)
    }
    
    /**
     * Gets all roles that are lower in hierarchy than the given role
     */
    fun getSubordinateRoles(role: UserRole): List<UserRole> {
        val currentLevel = getRoleHierarchyLevel(role)
        return UserRole.values().filter { 
            getRoleHierarchyLevel(it) < currentLevel 
        }
    }
    
    // ========================================
    // UTILITY FUNCTIONS
    // ========================================
    
    /**
     * Gets a human-readable description of what a role can do
     */
    fun getRoleDescription(role: UserRole): String {
        return when (role) {
            UserRole.SYSTEM_OWNER -> "Full system access, can manage all businesses and users"
            UserRole.SUPERADMIN -> "Can manage multiple businesses and their users"
            UserRole.ADMIN -> "Can manage users, vehicles, and reports within their business"
            UserRole.OPERATOR -> "Can operate vehicles, create incidents, and complete checklists"
        }
    }
    
    /**
     * Gets role-specific color for UI theming
     */
    fun getRoleColor(role: UserRole): String {
        return when (role) {
            UserRole.SYSTEM_OWNER -> "#FF6B35" // Orange-red
            UserRole.SUPERADMIN -> "#E74C3C"   // Red
            UserRole.ADMIN -> "#3498DB"        // Blue
            UserRole.OPERATOR -> "#27AE60"     // Green
        }
    }
}

/**
 * Permissions enum for role-based access control
 */
enum class Permission {
    // Admin permissions
    VIEW_ADMIN_DASHBOARD,
    MANAGE_USERS,
    MANAGE_VEHICLES,
    VIEW_REPORTS,
    CREATE_CHECKLISTS,
    
    // Super admin permissions
    MANAGE_BUSINESS,
    
    // System owner permissions
    SYSTEM_ADMINISTRATION,
    
    // General permissions
    OPERATE_VEHICLE,
    VIEW_INCIDENTS,
    CREATE_INCIDENTS,
    VIEW_CHECKLISTS
}

/**
 * Navigation items enum for role-based navigation
 */
enum class NavigationItem {
    // Admin navigation
    ADMIN_DASHBOARD,
    USER_MANAGEMENT,
    VEHICLE_MANAGEMENT,
    REPORTS,
    BUSINESS_MANAGEMENT,
    SYSTEM_SETTINGS,
    
    // General navigation
    OPERATOR_DASHBOARD,
    VEHICLES,
    INCIDENTS,
    CHECKLISTS
} 