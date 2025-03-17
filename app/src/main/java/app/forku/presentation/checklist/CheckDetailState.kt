package app.forku.presentation.checklist

data class CheckDetailState(
    val isLoading: Boolean = false,
    val check: PreShiftCheckState? = null,
    val error: String? = null
) 