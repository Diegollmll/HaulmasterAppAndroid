package app.forku.domain.model.report

import java.time.LocalDate

/**
 * Filter options for reports
 */
data class ReportFilter(
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val businessId: String? = null,
    val siteId: String? = null,
    val userId: String? = null,
    val vehicleId: String? = null,
    val status: String? = null,
    val type: String? = null,
    val severity: String? = null,
    val includeDetails: Boolean? = null // For checklist reports: true = "Con detalles", false = "Sin detalles"
) {
    /**
     * Check if any filters are applied
     */
    fun hasFilters(): Boolean {
        return dateFrom != null || dateTo != null || businessId != null || 
               siteId != null || userId != null || vehicleId != null || 
               status != null || type != null || severity != null ||
               includeDetails != null
    }
    
    /**
     * Get a summary of applied filters for display
     */
    fun getFilterSummary(): String {
        val filters = mutableListOf<String>()
        
        if (dateFrom != null && dateTo != null) {
            filters.add("Date: $dateFrom to $dateTo")
        } else if (dateFrom != null) {
            filters.add("From: $dateFrom")
        } else if (dateTo != null) {
            filters.add("Until: $dateTo")
        }
        
        businessId?.let { filters.add("Business: $it") }
        siteId?.let { filters.add("Site: $it") }
        userId?.let { filters.add("User: $it") }
        vehicleId?.let { filters.add("Vehicle: $it") }
        status?.let { filters.add("Status: $it") }
        type?.let { filters.add("Type: $it") }
        severity?.let { filters.add("Severity: $it") }
        includeDetails?.let { 
            filters.add("Details: ${if (it) "With Details" else "No Details"}") 
        }
        
        return if (filters.isEmpty()) "No filters applied" else filters.joinToString(", ")
    }
} 