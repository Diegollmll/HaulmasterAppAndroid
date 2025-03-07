package app.forku.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.user.AuthRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.usecase.checklist.GetLastPreShiftCheckCurrentUserUseCase
import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import app.forku.domain.usecase.vehicle.GetVehicleUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val sessionRepository: SessionRepository,
    private val checklistRepository: ChecklistRepository,
    private val authRepository: AuthRepository,
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase,
    private val getVehicleUseCase: GetVehicleUseCase,
    private val getLastPreShiftCheckCurrentUserUseCase: GetLastPreShiftCheckCurrentUserUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()
    
    init {
        viewModelScope.launch {
            loadDashboard(showLoading = true)
        }
    }
    
    private suspend fun loadDashboard(showLoading: Boolean = false) {
        try {
            if (showLoading) {
                _state.update { it.copy(isLoading = true) }
            }
            
            val currentUser = authRepository.getCurrentUser()
                ?: throw Exception("User not authenticated")

            // Obtenemos la sesión actual
            val currentSession = sessionRepository.getCurrentSession()

            // Obtenemos el vehículo de la sesión activa si existe
            val sessionVehicle = currentSession?.let { 
                getVehicleUseCase(it.vehicleId)
            }
            
            // Obtenemos el último check
            val lastPreShiftCheck = try {
                getLastPreShiftCheckCurrentUserUseCase()
            } catch (e: Exception) {
                null
            }
            
            // Obtenemos el vehículo del último check si no hay sesión activa
            val checkVehicle = if (currentSession == null) {
                lastPreShiftCheck?.let { 
                    getVehicleUseCase(it.vehicleId)
                }
            } else null
            
            // Log session vehicle details
            android.util.Log.d("appflow DashboardViewModel", "Session Vehicle: $sessionVehicle")
            sessionVehicle?.let {
                android.util.Log.d("appflow DashboardViewModel", """
                    Session Vehicle Properties:
                    - ID: ${it.id}
                    - Codename: ${it.codename}
                    - Status: ${it.status}
                    - Photo URL: ${it.photoModel}          
                """.trimIndent())
            }

            // Log check vehicle details  
            android.util.Log.d("appflow DashboardViewModel", "Check Vehicle: $checkVehicle")
            checkVehicle?.let {
                android.util.Log.d("appflow DashboardViewModel", """
                    Check Vehicle Properties:
                    - ID: ${it.id}
                    - Codename: ${it.codename} 
                    - Status: ${it.status}
                    - Photo URL: ${it.photoModel}
                """.trimIndent())
            }




            _state.update {
                it.copy(
                    isLoading = false,
                    user = currentUser,
                    isAuthenticated = true,
                    lastPreShiftCheck = lastPreShiftCheck,
                    currentSession = currentSession,
                    displayVehicle = sessionVehicle ?: checkVehicle,
                    error = null
                )
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "Error al cargar dashboard: ${e.message}"
                )
            }
        }
    }

    // Refresh silencioso (sin loading) cuando volvemos a la pantalla
    fun refresh() {
        viewModelScope.launch {
            loadDashboard(showLoading = false)
        }
    }

    // Refresh con loading para pull-to-refresh o acciones explícitas del usuario
    fun refreshWithLoading() {
        viewModelScope.launch {
            loadDashboard(showLoading = true)
        }
    }

    fun endCurrentSession() {
        viewModelScope.launch {
            try {
                android.util.Log.d("appflow DashboardViewModel", "Starting endCurrentSession")
                _state.update { it.copy(isLoading = true) }
                
                val currentSession = state.value.currentSession
                android.util.Log.d("appflow DashboardViewModel", "Current session: $currentSession")
                
                if (currentSession != null) {
                    android.util.Log.d("appflow DashboardViewModel", "Ending session with ID: ${currentSession.id}")
                    sessionRepository.endSession(currentSession.id)
                    
                    // Actualizar el estado después de finalizar la sesión
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            currentSession = null,
                            displayVehicle = null,
                            error = null
                        )
                    }
                    
                    android.util.Log.d("appflow DashboardViewModel", "Session ended successfully, reloading dashboard")
                    // Recargar el dashboard para obtener el nuevo estado
                    loadDashboard(showLoading = false)
                }
            } catch (e: Exception) {
                android.util.Log.e("appflow DashboardViewModel", "Error ending session", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al finalizar sesión: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 