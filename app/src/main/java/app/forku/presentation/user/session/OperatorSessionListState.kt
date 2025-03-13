package app.forku.presentation.user.session

import app.forku.presentation.dashboard.OperatorSessionInfo

data class OperatorSessionListState(
    val operators: List<OperatorSessionInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) 