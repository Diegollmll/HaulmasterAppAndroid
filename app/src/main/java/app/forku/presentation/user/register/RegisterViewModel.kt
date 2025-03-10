package app.forku.presentation.user.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Patterns

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.EmailChanged -> {
                _state.value = _state.value.copy(
                    email = event.email,
                    error = null
                )
            }
            is RegisterEvent.PasswordChanged -> {
                _state.value = _state.value.copy(
                    password = event.password,
                    error = null
                )
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _state.value = _state.value.copy(
                    confirmPassword = event.confirmPassword,
                    error = null
                )
            }
            is RegisterEvent.FirstNameChanged -> {
                _state.value = _state.value.copy(
                    firstName = event.firstName,
                    error = null
                )
            }
            is RegisterEvent.LastNameChanged -> {
                _state.value = _state.value.copy(
                    lastName = event.lastName,
                    error = null
                )
            }
            is RegisterEvent.TogglePasswordVisibility -> {
                _state.value = _state.value.copy(
                    isPasswordVisible = !_state.value.isPasswordVisible
                )
            }
            is RegisterEvent.ToggleConfirmPasswordVisibility -> {
                _state.value = _state.value.copy(
                    isConfirmPasswordVisible = !_state.value.isConfirmPasswordVisible
                )
            }
            is RegisterEvent.Submit -> {
                submitRegistration()
            }
        }
    }

    private fun submitRegistration() {
        val state = _state.value
        
        // Validate inputs
        if (state.firstName.isBlank()) {
            _state.value = state.copy(error = "First name is required")
            return
        }
        if (state.lastName.isBlank()) {
            _state.value = state.copy(error = "Last name is required")
            return
        }
        if (!isValidEmail(state.email)) {
            _state.value = state.copy(error = "Invalid email address")
            return
        }
        if (state.password.length < 8) {
            _state.value = state.copy(error = "Password must be at least 8 characters")
            return
        }
        if (state.password != state.confirmPassword) {
            _state.value = state.copy(error = "Passwords do not match")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = state.copy(isLoading = true)
                
                val result = userRepository.register(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    email = state.email,
                    password = state.password
                )

                _state.value = when {
                    result.isSuccess -> state.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    else -> state.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Registration failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Registration failed"
                )
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
} 