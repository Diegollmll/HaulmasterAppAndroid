package app.forku.presentation.gogroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.gogroup.GOGroupRole
import app.forku.domain.usecase.gogroup.role.GetGroupRolesUseCase
import app.forku.domain.usecase.gogroup.role.ManageGroupRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupRoleManagementViewModel @Inject constructor(
    private val getGroupRolesUseCase: GetGroupRolesUseCase,
    private val manageGroupRoleUseCase: ManageGroupRoleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GroupRoleManagementState())
    val state: StateFlow<GroupRoleManagementState> = _state

    fun loadRolesForGroup(groupName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getGroupRolesUseCase.getRolesByGroup(groupName)
                .onSuccess { roles ->
                    _state.update { it.copy(
                        roles = roles,
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

    fun assignRoleToGroup(groupName: String, roleName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            manageGroupRoleUseCase.assignRoleToGroup(groupName, roleName)
                .onSuccess { groupRole ->
                    _state.update { state ->
                        state.copy(
                            roles = state.roles + groupRole,
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

    fun updateRoleStatus(groupRole: GOGroupRole, newIsActive: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            manageGroupRoleUseCase.updateGroupRole(groupRole, newIsActive)
                .onSuccess { updatedRole ->
                    _state.update { state ->
                        state.copy(
                            roles = state.roles.map { 
                                if (it.groupName == updatedRole.groupName && 
                                    it.roleName == updatedRole.roleName) {
                                    updatedRole
                                } else it 
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

    fun removeRoleFromGroup(groupName: String, roleName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            manageGroupRoleUseCase.removeRoleFromGroup(groupName, roleName)
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            roles = state.roles.filterNot { 
                                it.groupName == groupName && it.roleName == roleName 
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

data class GroupRoleManagementState(
    val roles: List<GOGroupRole> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 