package app.forku.presentation.incident

import app.forku.domain.model.incident.IncidentType
import android.net.Uri
import app.forku.presentation.incident.model.IncidentFormSection

data class IncidentReportState(
    // Common fields
    val type: IncidentType? = null,
    val date: Long = System.currentTimeMillis(),
    val location: String = "",
    val weather: String = "",
    val description: String = "",
    
    // Session info
    val sessionId: String? = null,
    
    // People involved
    val operatorId: String? = null,
    val othersInvolved: List<String> = emptyList(),
    val injuries: String = "",
    val injuryLocations: List<String> = emptyList(),
    
    // Vehicle info
    val vehicleId: String? = null,
    val vehicleType: String = "",
    val loadType: String = "",
    val loadWeight: String = "",
    
    // Incident specific
    val activityAtTime: String = "",
    val immediateActions: List<String> = emptyList(),
    val proposedSolutions: List<String> = emptyList(),
    val photos: List<Uri> = emptyList(),
    
    // Form state
    val currentSection: IncidentFormSection = IncidentFormSection.BasicInfo,
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null,
    val showSuccessDialog: Boolean = false
) 