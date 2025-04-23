package app.forku.presentation.tour

data class TourState(
    val currentPage: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCsrfTokenInitialized: Boolean = false
)

sealed class TourEvent {
    data object NextPage : TourEvent()
    data object PreviousPage : TourEvent()
    data object FinishTour : TourEvent()
    data object Register : TourEvent()
    data object Login : TourEvent()
    data object InitializeCsrfToken : TourEvent()
} 