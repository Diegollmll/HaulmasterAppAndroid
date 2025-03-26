package app.forku.presentation.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Dashboard : Screen("dashboard")
    data object QRScanner : Screen("qr_scanner")
    data object VehicleProfile : Screen("vehicle_profile/{vehicleId}")
    data object Checklist : Screen("checklist/{vehicleId}")
    data object CheckDetail : Screen("check_detail/{checkId}") {
        fun createRoute(checkId: String): String = "check_detail/$checkId"
    }
    data object Profile : Screen("profile?operatorId={operatorId}") {
        fun createRoute(operatorId: String? = null): String {
            return if (operatorId != null) {
                "profile?operatorId=$operatorId"
            } else {
                "profile"
            }
        }
    }
    data object IncidentList : Screen("incident_list?userId={userId}&source={source}") {
        fun createRoute(userId: String? = null, source: String? = null): String {
            return buildString {
                append("incident_list")
                if (userId != null || source != null) {
                    append("?")
                    if (userId != null) {
                        append("userId=$userId")
                        if (source != null) append("&")
                    }
                    if (source != null) {
                        append("source=$source")
                    }
                }
            }
        }
    }
    data object OperatorsCICOHistory : Screen("operators_cico_history?operatorId={operatorId}&source={source}") {
        fun createRoute(operatorId: String? = null, source: String? = null): String {
            android.util.Log.e("appflow", "OperatorsCICOHistory createRoute operatorId: $operatorId, source: $source")
            return buildString {
                append("operators_cico_history")
                if (operatorId != null || source != null) {
                    append("?")
                    if (operatorId != null) {
                        append("operatorId=$operatorId")
                        if (source != null) append("&")
                    }
                    if (source != null) {
                        append("source=$source")
                    }
                }
            }
        }
    }
    data object VehiclesList : Screen("vehicles")
    data object SafetyReporting : Screen("safety_reporting")
    data object PerformanceReport : Screen("performance_report")
    data object IncidentDetail : Screen("incident_detail/{incidentId}")
    data object Tour : Screen("tour")
    data object AdminDashboard : Screen("admin_dashboard")
    data object OperatorsList : Screen("operator_session_list")
    data object Notifications : Screen("notifications")
    data object AllChecklist : Screen("all_checklist")
    data object SafetyAlerts : Screen("safety_alerts")
    data object CertificationsList : Screen("certifications?userId={userId}") {
        fun createRoute(userId: String? = null): String =
            "certifications" + (userId?.let { "?userId=$it" } ?: "")
    }
    data object CertificationDetail : Screen("certification/{certificationId}") {
        fun createRoute(certificationId: String): String =
            "certification/$certificationId"
    }
    data object CertificationEdit : Screen("certification/{certificationId}/edit") {
        fun createRoute(certificationId: String): String =
            "certification/$certificationId/edit"
    }
    data object CertificationCreate : Screen("certification/create")

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