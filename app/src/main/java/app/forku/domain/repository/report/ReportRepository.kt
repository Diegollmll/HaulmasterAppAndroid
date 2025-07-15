package app.forku.domain.repository.report

import app.forku.domain.model.report.ReportFilter
import app.forku.domain.model.report.ReportType
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.checklist.ChecklistAnswer
import app.forku.domain.model.incident.Incident
import app.forku.domain.model.certification.Certification

/**
 * Repository for generating reports by aggregating data from existing repositories
 */
interface ReportRepository {
    
    /**
     * Generate vehicles report with filtering
     */
    suspend fun getVehiclesReport(filter: ReportFilter): List<VehicleReportItem>
    
    /**
     * Generate checklists report with filtering  
     */
    suspend fun getChecklistsReport(filter: ReportFilter): List<ChecklistReportItem>
    
    /**
     * Generate incidents report with filtering
     */
    suspend fun getIncidentsReport(filter: ReportFilter): List<IncidentReportItem>
    
    /**
     * Generate certifications report with filtering
     */
    suspend fun getCertificationsReport(filter: ReportFilter): List<CertificationReportItem>
    
    /**
     * Export report data to specified format
     */
    suspend fun exportReport(
        reportType: ReportType,
        filter: ReportFilter,
        format: app.forku.domain.model.report.ExportFormat
    ): ByteArray
    
    /**
     * Get available filter options for a specific report type
     */
    suspend fun getFilterOptions(reportType: ReportType): ReportFilterOptions
}

/**
 * Vehicle report item with comprehensive data
 */
data class VehicleReportItem(
    val vehicleId: String,
    val vehicleName: String,
    val vehicleType: String,
    val status: String,
    val lastSessionDate: String?,
    val lastSessionUser: String?,
    val totalSessions: Int,
    val businessName: String,
    val siteName: String,
    
    // Additional vehicle details
    val model: String,
    val serialNumber: String,
    val description: String,
    val energySource: String,
    val categoryName: String,
    val currentHourMeter: String?,
    val hasIssues: Boolean
)

/**
 * Individual checklist item result
 */
data class ChecklistItemResult(
    val question: String,
    val result: String, // PASS/FAIL/N/A
    val comment: String = ""
)

/**
 * Checklist report item with comprehensive data
 */
data class ChecklistReportItem(
    // Basic identification
    val checklistId: String,
    val checklistName: String,
    val vehicleCodename: String,
    val userName: String,
    val completedDate: String,
    val status: String,
    
    // Statistics
    val passedItems: Int,
    val failedItems: Int,
    val totalItems: Int,
    
    // Location and context
    val businessName: String,
    val siteName: String,
    val locationCoordinates: String?,
    
    // Detailed information
    val reportDate: String,
    val reportTime: String,
    val vehicleType: String,
    val vehicleCategory: String,
    val vehicleModel: String,
    val vehicleSerialNumber: String,
    val checklistVersion: String,
    val appVersion: String = "1.0",
    
    // Duration and timing
    val startDateTime: String,
    val endDateTime: String,
    val duration: String,
    
    // Additional fields
    val checklistItems: List<ChecklistItemResult> = emptyList(),
    val userComments: String = "",
    val photoVideoUrl: String = "",
    val issueFlag: String = "N",
    val vehicleRemovedFromService: String = "N",
    val summary: String = ""
)

/**
 * Incident report item with comprehensive data including type-specific fields
 */
data class IncidentReportItem(
    // Common fields
    val incidentId: String,
    val type: String,
    val severity: String,
    val reportedDate: String,
    val reportedBy: String,
    val vehicleId: String?,
    val vehicleName: String?,
    val description: String,
    val status: String,
    val businessName: String,
    val siteName: String,
    val locationDetails: String,
    val weather: String?,
    
    // Common load information
    val isLoadCarried: String?, // "Yes"/"No"/null
    val loadWeight: String?,
    val loadBeingCarried: String?,
    val othersInvolved: String?,
    
    // Collision-specific fields
    val collisionType: String?, // Comma-separated list
    val injurySeverity: String?,
    val injuryLocation: String?, // Comma-separated list
    val damageOccurrence: String?, // Comma-separated list
    
    // Near Miss-specific fields
    val nearMissType: String?, // Comma-separated list
    val potentialImpact: String?, // Derived from severity and type
    
    // Hazard-specific fields
    val hazardType: String?,
    val potentialConsequences: String?, // Comma-separated list
    val preventiveMeasures: String?, // Comma-separated list
    
    // Vehicle Fail-specific fields
    val failureType: String?, // Comma-separated list
    val vehicleFailDamage: String?, // Comma-separated list
    val environmentalImpact: String?, // Comma-separated list
    
    // Common action fields
    val immediateActions: String?, // Comma-separated list
    val longTermSolutions: String?, // Comma-separated list
    val contributingFactors: String? // Comma-separated list
)

/**
 * Certification report item with aggregated data
 */
data class CertificationReportItem(
    val certificationId: String,
    val userName: String,
    val certificationType: String,
    val issueDate: String,
    val expiryDate: String,
    val status: String,
    val daysUntilExpiry: Int?,
    val vehicleTypes: List<String>,
    val businessName: String
)

/**
 * Available filter options for a report type
 */
data class ReportFilterOptions(
    val businesses: List<FilterOption>,
    val sites: List<FilterOption>,
    val users: List<FilterOption>,
    val vehicles: List<FilterOption>,
    val statuses: List<FilterOption>,
    val types: List<FilterOption>,
    val severities: List<FilterOption>,
    val dateRanges: List<FilterOption>,
    val detailOptions: List<FilterOption> // For checklist reports: Con detalles/Sin detalles
)

/**
 * Filter option with display name and value
 */
data class FilterOption(
    val value: String,
    val displayName: String
) 