package app.forku.presentation.checklist

/**
 * UI State for the All Checklist Screen
 * Manages pagination, loading states, and checklist data
 */
data class AllChecklistState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val checks: List<PreShiftCheckState> = emptyList(),
    val currentPage: Int = 1,
    val itemsPerPage: Int = 10,
    val hasMoreItems: Boolean = true
) 