package app.forku.presentation.gogroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.gogroup.GOGroup
import app.forku.domain.usecase.gogroup.group.GetGroupsUseCase
import app.forku.domain.usecase.gogroup.group.ManageGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupManagementViewModel @Inject constructor(
    private val getGroupsUseCase: GetGroupsUseCase,
    private val manageGroupUseCase: ManageGroupUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GroupManagementState())
    val state: StateFlow<GroupManagementState> = _state

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getGroupsUseCase()
                .onSuccess { groups ->
                    _state.update { it.copy(
                        groups = groups,
                        isLoading = false
                    )}
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        error = error.message,
                        isLoading = false
                    )}
                }
        }
    }

    fun createGroup(name: String, description: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            manageGroupUseCase.createGroup(name, description)
                .onSuccess { group ->
                    _state.update { state ->
                        state.copy(
                            groups = state.groups + group,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        error = error.message,
                        isLoading = false
                    )}
                }
        }
    }

    fun updateGroup(group: GOGroup, newDescription: String?, newIsActive: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            manageGroupUseCase.updateGroup(group, newDescription, newIsActive)
                .onSuccess { updatedGroup ->
                    _state.update { state ->
                        state.copy(
                            groups = state.groups.map { 
                                if (it.name == updatedGroup.name) updatedGroup else it 
                            },
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        error = error.message,
                        isLoading = false
                    )}
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class GroupManagementState(
    val groups: List<GOGroup> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 