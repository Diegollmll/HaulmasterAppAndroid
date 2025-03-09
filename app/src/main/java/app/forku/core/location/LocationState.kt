package app.forku.core.location

import com.google.android.gms.common.api.ResolvableApiException

data class LocationState(
    val location: String? = null,
    val error: String? = null,
    val hasLocationPermission: Boolean = false,
    val locationSettingsException: ResolvableApiException? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) 