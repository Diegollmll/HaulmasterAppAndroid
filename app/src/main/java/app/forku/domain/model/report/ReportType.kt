package app.forku.domain.model.report

/**
 * Types of reports available in the system
 */
enum class ReportType(
    val displayName: String,
    val description: String,
    val icon: String = "📊"
) {
    VEHICLES(
        displayName = "Vehicles Report",
        description = "Vehicle status, type, and last session data",
        icon = "🚗"
    ),
    CHECKLISTS(
        displayName = "Checklists Report", 
        description = "Checklist completion by user, vehicle, and status",
        icon = "✅"
    ),
    INCIDENTS(
        displayName = "Incidents Report",
        description = "Incident type, severity, and date analysis",
        icon = "⚠️"
    ),
    CERTIFICATIONS(
        displayName = "Certifications Report",
        description = "Certification validity, user, and status tracking",
        icon = "🏆"
    )
} 