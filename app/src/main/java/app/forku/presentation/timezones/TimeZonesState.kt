package app.forku.presentation.timezones

data class TimeZonesState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val timeZones: List<TimeZoneInfo> = emptyList(),
    val filteredTimeZones: List<TimeZoneInfo> = emptyList(),
    val selectedTimeZone: TimeZoneInfo? = null,
    val searchQuery: String = ""
) 