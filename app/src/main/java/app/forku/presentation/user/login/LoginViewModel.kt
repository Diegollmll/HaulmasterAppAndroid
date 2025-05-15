package app.forku.presentation.user.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.user.User
import app.forku.domain.repository.user.UserRepository
import app.forku.core.auth.TokenErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenErrorHandler: TokenErrorHandler
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _state.value = LoginState.Loading
                
                Log.d("LoginViewModel", "Attempting login for user: $username")
                val result = userRepository.login(username, password)
                
                result.fold(
                    onSuccess = { user ->
                        // Reset token error handler state on successful login
                        tokenErrorHandler.resetAuthenticationState()
                        
                        Log.d("LoginViewModel", "Login successful")
                        _state.value = LoginState.Success(user)
                    },
                    onFailure = { exception ->
                        Log.e("LoginViewModel", "Login failed", exception)
                        _state.value = LoginState.Error(exception.message ?: "Login failed")
                    }
                )
                
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login failed with exception", e)
                _state.value = LoginState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }
}

