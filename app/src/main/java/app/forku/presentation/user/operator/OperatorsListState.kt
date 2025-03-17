package app.forku.presentation.user.operator

import app.forku.presentation.dashboard.OperatorSessionInfo

data class OperatorsListState(
    val operators: List<OperatorSessionInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) 