package app.forku.presentation.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Dashboard : Screen("dashboard")
    data object QRScanner : Screen("qr_scanner")
    data object VehicleProfile : Screen("vehicle/{vehicleId}?businessId={businessId}") {
        fun createRoute(vehicleId: String, businessId: String? = null): String =
            "vehicle/$vehicleId${if (businessId != null) "?businessId=$businessId" else ""}"
    }
    data object Checklist : Screen("checklist/{vehicleId}") {
        fun createRoute(vehicleId: String, checkId: String? = null, fromScanner: Boolean = false): String = buildString {
            append("checklist/$vehicleId")
            if (checkId != null || fromScanner) {
                append("?")
                if (checkId != null) {
                    append("checkId=$checkId")
                    if (fromScanner) append("&")
                }
                if (fromScanner) {
                    append("fromScanner=true")
                }
            }
        }
    }
    data object CheckDetail : Screen("check/{checkId}") {
        fun createRoute(checkId: String): String = "check/$checkId"
    }
    data object Profile : Screen("profile?operatorId={operatorId}") {
        fun createRoute(operatorId: String? = null): String =
            "profile${if (operatorId != null) "?operatorId=$operatorId" else ""}"
    }
    data object IncidentList : Screen("incidents?userId={userId}&source={source}") {
        fun createRoute(userId: String? = null, source: String? = null): String = buildString {
            append("incidents")
            val params = mutableListOf<String>()
            userId?.let { params.add("userId=$it") }
            source?.let { params.add("source=$it") }
            if (params.isNotEmpty()) {
                append("?")
                append(params.joinToString("&"))
            }
        }
    }
    data object OperatorsCICOHistory : Screen("cico_history?operatorId={operatorId}&source={source}") {
        fun createRoute(operatorId: String? = null, source: String? = null): String = buildString {
            append("cico_history")
            val params = mutableListOf<String>()
            operatorId?.let { params.add("operatorId=$it") }
            source?.let { params.add("source=$it") }
            if (params.isNotEmpty()) {
                append("?")
                append(params.joinToString("&"))
            }
        }
    }
    data object VehiclesList : Screen("vehicles")
    data object VehicleCategories : Screen("vehicle_categories")
    data object VehicleTypes : Screen("vehicle_types")
    data object SafetyReporting : Screen("safety_reporting")
    data object PerformanceReport : Screen("performance_report")
    data object IncidentDetail : Screen("incident/{incidentId}")
    data object Tour : Screen("tour")
    data object AdminDashboard : Screen("admin_dashboard")
    data object SystemOwnerDashboard : Screen("system_owner_dashboard")
    data object SuperAdminDashboard : Screen("superadmin_dashboard")
    data object OperatorsList : Screen("operators")
    data object Notifications : Screen("notifications")
    data object AllChecklist : Screen("all_checklist")
    data object SafetyAlerts : Screen("safety_alerts")
    data object EnergySources : Screen("energy_sources")
    data object CertificationsList : Screen("certifications?userId={userId}") {
        fun createRoute(userId: String? = null): String =
            "certifications${if (userId != null) "?userId=$userId" else ""}"
    }
    data object CertificationDetail : Screen("certification/{certificationId}") {
        fun createRoute(certificationId: String): String = "certification/$certificationId"
    }
    data object CertificationEdit : Screen("certification/edit/{certificationId}") {
        fun createRoute(certificationId: String): String = "certification/edit/$certificationId"
    }
    data object CertificationCreate : Screen("certification/create")
    
    // SuperAdmin specific routes
    data object UserManagement : Screen("user_management")
    data object RoleManagement : Screen("role_management")
    data object PermissionsManagement : Screen("permissions_management")
    data object BusinessManagement : Screen("business_management")
    data object AddUser : Screen("add_user")
    data object AdminManagement : Screen("admin_management")
    data object AddVehicle : Screen("add_vehicle")
    data object MaintenanceSchedule : Screen("maintenance_schedule")
    data object VehicleReports : Screen("vehicle_reports")
    data object SystemSettings : Screen("system_settings")
    data object UserPreferencesSetup : Screen("user_preferences_setup?showBack={showBack}") {
        fun createRoute(showBack: Boolean = false): String =
            "user_preferences_setup?showBack=$showBack"
    }
    data object Subscriptions : Screen("subscriptions")
    data object SystemBackup : Screen("system_backup")
    data object TimeZones : Screen("timezones")
    data object Countries : Screen("countries")
    data object EditVehicle : Screen("edit_vehicle/{vehicleId}?businessId={businessId}") {
        fun createRoute(vehicleId: String, businessId: String?) = "edit_vehicle/$vehicleId" + (businessId?.let { "?businessId=$it" } ?: "")
    }

    // Admin Specific Vehicle Routes
    data object AdminVehiclesList : Screen("admin_vehicles")
    data object AdminVehicleProfile : Screen("admin_vehicle/{vehicleId}") {
        fun createRoute(vehicleId: String) = "admin_vehicle/$vehicleId"
    }
    
    // Checklist Management Routes
    data object ChecklistCategories : Screen("checklist_categories")
    data object ChecklistSubcategories : Screen("checklist_subcategories") {
        fun createRoute(): String = "checklist_subcategories"
    }
    data object Questionnaires : Screen("questionaries")
    data object QuestionaryItems : Screen("questionary_items?questionaryId={questionaryId}&isEditable={isEditable}") {
        fun createRoute(questionaryId: String? = null, isEditable: Boolean = true) =
            "questionary_items" + (questionaryId?.let { "?questionaryId=$it" } ?: "?questionaryId=") + "&isEditable=$isEditable"
    }
    data object EditChecklistCategory : Screen("edit_checklist_category/{categoryId}") {
        fun createRoute(categoryId: String) = "edit_checklist_category/$categoryId"
    }
    // TODO: Re-implement with proper ChecklistItem system
    // data object EditQuestionaryChecklistItemCategory : Screen("edit_questionary_checklist_item_category/{categoryId}") {
    //     fun createRoute(categoryId: String) = "edit_questionary_checklist_item_category/$categoryId"
    // }
    // data object QuestionaryChecklistItemSubcategory : Screen("checklist_subcategory/{categoryId}") {
    //     fun createRoute(categoryId: String): String = "checklist_subcategory/$categoryId"
    // }
    // data object EditQuestionaryChecklistItemSubcategory : Screen("edit_questionary_checklist_item_subcategory/{subcategoryId}?categoryId={categoryId}") {
    //     fun createRoute(subcategoryId: String?, categoryId: String? = null): String {
    //         return if (subcategoryId != null) {
    //             "edit_questionary_checklist_item_subcategory/$subcategoryId" + (categoryId?.let { "?categoryId=$it" } ?: "")
    //         } else {
    //             "edit_questionary_checklist_item_subcategory/new" + (categoryId?.let { "?categoryId=$it" } ?: "")
    //         }
    //     }
    // }
    data object QuestionaryChecklist : Screen("questionary_checklist") {
        fun createRoute(): String = "questionary_checklist"
    }
    data object Sites : Screen("sites/{businessId}") {
        fun createRoute(businessId: String): String = "sites/$businessId"
    }

    // Vehicle Management
    data object VehicleComponents : Screen("vehicle-components") {
        val title: String = "Vehicle Components"
    }

    data object GroupManagement : Screen("group_management")

    data object GroupRoleManagement : Screen("group_role_management/{groupName}") {
        const val GROUP_NAME_ARG = "groupName"

        fun createRoute(groupName: String) = "group_role_management/$groupName"
    }

    // Reports Module
    data object Reports : Screen("reports")

    companion object {
        fun Profile.withOperatorId(operatorId: String?) = 
            "profile" + (operatorId?.let { "?operatorId=$it" } ?: "")
        
        fun IncidentList.withParams(userId: String? = null, source: String? = null) =
            "incident_list" + listOfNotNull(
                userId?.let { "userId=$it" },
                source?.let { "source=$it" }
            ).joinToString("&", prefix = "?").let { if (it == "?") "" else it }
    }
}