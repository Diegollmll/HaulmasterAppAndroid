# 🎯 UserRoleManager - Centralized Role Management

## 📋 **OVERVIEW**

`UserRoleManager` is now the **single source of truth** for all role-related operations in the ForkU application. It centralizes functionality that was previously scattered across `RoleConverter` and other components.

## 🚀 **KEY FEATURES**

### **1. Role Determination & Conversion**
```kotlin
// Convert string to UserRole
val role = UserRoleManager.fromString("Administrator") // Returns UserRole.ADMIN
val role2 = UserRoleManager.fromString("operator") // Returns UserRole.OPERATOR

// Convert UserRole to display string
val displayText = UserRoleManager.toDisplayString(UserRole.ADMIN) // Returns "Administrator"

// Convert UserRole to API string
val apiString = UserRoleManager.toApiString(UserRole.ADMIN) // Returns "administrator"
```

### **2. Effective Role Determination**
```kotlin
// Determine the most appropriate role for a user in business context
val effectiveRole = UserRoleManager.getEffectiveRole(
    user = currentUser,
    userRoleItems = userRoleItemsFromAPI,
    businessRole = UserRole.OPERATOR, // Business assignment
    businessId = "business-123"
)
// Priority: UserRoleItems > User general role > Business role
```

### **3. Permissions & Access Control**
```kotlin
// Check specific permissions
val canManageUsers = UserRoleManager.hasPermission(
    userRole = UserRole.ADMIN,
    permission = Permission.MANAGE_USERS
) // Returns true

// Get all permissions for a role
val adminPermissions = UserRoleManager.getPermissionsForRole(UserRole.ADMIN)
// Returns: [VIEW_ADMIN_DASHBOARD, MANAGE_USERS, MANAGE_VEHICLES, VIEW_REPORTS, CREATE_CHECKLISTS]

// Check role types
val isAdmin = UserRoleManager.isAdminUser(UserRole.ADMIN) // Returns true
val isSuperUser = UserRoleManager.isSuperUser(UserRole.SUPERADMIN) // Returns true
val canManageMultipleBusiness = UserRoleManager.canManageMultipleBusinesses(UserRole.SYSTEM_OWNER) // Returns true
```

### **4. Navigation & Routing**
```kotlin
// Get appropriate dashboard route
val dashboardRoute = UserRoleManager.getDashboardRoute(UserRole.ADMIN)
// Returns: "admin_dashboard"

// Check navigation visibility
val shouldShowAdminMenu = UserRoleManager.shouldShowNavigationItem(
    role = UserRole.ADMIN,
    navigationItem = NavigationItem.USER_MANAGEMENT
) // Returns true
```

### **5. Role Hierarchy & Comparison**
```kotlin
// Get hierarchy level (higher = more permissions)
val adminLevel = UserRoleManager.getRoleHierarchyLevel(UserRole.ADMIN) // Returns 2
val operatorLevel = UserRoleManager.getRoleHierarchyLevel(UserRole.OPERATOR) // Returns 1

// Compare roles
val hasHigherPermissions = UserRoleManager.hasHigherOrEqualPermissions(
    UserRole.ADMIN, 
    UserRole.OPERATOR
) // Returns true

// Get subordinate roles
val subordinates = UserRoleManager.getSubordinateRoles(UserRole.ADMIN)
// Returns: [UserRole.OPERATOR]
```

### **6. Utility Functions**
```kotlin
// Get role description
val description = UserRoleManager.getRoleDescription(UserRole.ADMIN)
// Returns: "Can manage users, vehicles, and reports within their business"

// Get role color for UI
val color = UserRoleManager.getRoleColor(UserRole.ADMIN)
// Returns: "#3498DB" (Blue)
```

## 🔧 **MIGRATION FROM RoleConverter**

### **Before (RoleConverter - Deprecated)**
```kotlin
// ❌ Old way - scattered functionality
val role = RoleConverter.fromString("admin")
val displayText = RoleConverter.toDisplayString(role)
val route = RoleConverter.getDashboardRouteForRole(role)
```

### **After (UserRoleManager - Centralized)**
```kotlin
// ✅ New way - centralized functionality
val role = UserRoleManager.fromString("admin")
val displayText = UserRoleManager.toDisplayString(role)
val route = UserRoleManager.getDashboardRoute(role)

// ✅ Plus many more capabilities
val permissions = UserRoleManager.getPermissionsForRole(role)
val canManageUsers = UserRoleManager.hasPermission(role, Permission.MANAGE_USERS)
val effectiveRole = UserRoleManager.getEffectiveRole(user, userRoleItems, businessRole)
```

## 📊 **PERMISSION SYSTEM**

### **Available Permissions**
```kotlin
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
```

### **Navigation Items**
```kotlin
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
```

## 🎯 **ROLE PRIORITY SYSTEM**

The `getEffectiveRole()` function uses this priority order:

1. **UserRoleItems** (from API) - Most accurate, includes business context
2. **User general role** - Fallback from user object
3. **Business role assignment** - Usually "OPERATOR" for business assignments
4. **Default** - UserRole.OPERATOR

## 🔄 **BACKWARD COMPATIBILITY**

`RoleConverter` is maintained as a deprecated wrapper:

```kotlin
// Still works but shows deprecation warnings
val role = RoleConverter.fromString("admin") // ⚠️ Deprecated
val displayText = RoleConverter.toDisplayString(role) // ⚠️ Deprecated

// Recommended migration
val role = UserRoleManager.fromString("admin") // ✅ Recommended
val displayText = UserRoleManager.toDisplayString(role) // ✅ Recommended
```

## 🎉 **BENEFITS OF CENTRALIZATION**

1. **Single Source of Truth** - All role logic in one place
2. **Enhanced Functionality** - Permissions, hierarchy, navigation
3. **Better Testing** - Centralized logic is easier to test
4. **Consistent Behavior** - No scattered role handling
5. **Future-Proof** - Easy to extend with new role features
6. **Type Safety** - Enum-based permissions and navigation items
7. **Business Context Aware** - Handles complex role determination

## 📝 **USAGE IN VIEWMODELS**

```kotlin
class SomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    fun checkUserAccess() {
        val currentUser = userRepository.getCurrentUser()
        val effectiveRole = UserRoleManager.getEffectiveRole(
            user = currentUser,
            userRoleItems = currentUser.userRoleItems,
            businessRole = getBusinessRole(currentUser.id)
        )
        
        val canManageUsers = UserRoleManager.hasPermission(
            effectiveRole, 
            Permission.MANAGE_USERS
        )
        
        if (canManageUsers) {
            // Show user management UI
        }
    }
}
```

## 🔧 **USAGE IN UI COMPONENTS**

```kotlin
@Composable
fun NavigationMenu(userRole: UserRole) {
    if (UserRoleManager.shouldShowNavigationItem(userRole, NavigationItem.ADMIN_DASHBOARD)) {
        NavigationItem(
            text = "Admin Dashboard",
            onClick = { 
                navController.navigate(UserRoleManager.getDashboardRoute(userRole))
            }
        )
    }
    
    if (UserRoleManager.hasPermission(userRole, Permission.MANAGE_USERS)) {
        NavigationItem(
            text = "Manage Users",
            onClick = { /* Navigate to user management */ }
        )
    }
}
```

---

**🎯 Result**: All role-related functionality is now centralized, consistent, and easily extensible while maintaining backward compatibility. 