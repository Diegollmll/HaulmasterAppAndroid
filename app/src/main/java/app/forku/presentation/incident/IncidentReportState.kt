package app.forku.presentation.incident

import app.forku.domain.model.incident.IncidentType
import android.net.Uri
import app.forku.presentation.incident.model.IncidentFormSection
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.common.api.ResolvableApiException

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
    val showSuccessDialog: Boolean = false,
    
    // Location permission state
    val hasLocationPermission: Boolean = false,
    val locationCoordinates: String? = null,
    
    // Location settings state
    val showLocationSettingsDialog: Boolean = false,
    val locationSettingsException: ResolvableApiException? = null
)
