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
    private val loginUseCase: app.forku.domain.usecase.user.LoginUseCase,
    private val userRepository: UserRepository,
    private val tokenErrorHandler: TokenErrorHandler,
    private val businessContextManager: app.forku.core.business.BusinessContextManager,
    private val userPreferencesRepository: app.forku.domain.repository.user.UserPreferencesRepository,
    private val authDataStore: app.forku.data.datastore.AuthDataStore // <-- Inyectar AuthDataStore
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "[LOGIN] login() called with username: $username, password: ${password.map { '*' }}")
                _state.value = LoginState.Loading
                Log.d("LoginViewModel", "[LOGIN] Llamando a loginUseCase (flujo centralizado)")
                val result = loginUseCase(username, password)
                Log.d("LoginViewModel", "[LOGIN] loginUseCase result: $result")
                result.fold(
                    onSuccess = { user ->
                        Log.d("LoginViewModel", "[LOGIN] Login success, user: $user")
                        try {
                            Log.d("LoginViewModel", "[LOGIN] Guardando usuario y tokens en AuthDataStore...")
                            Log.d("LoginViewModel", "authDataStore instance: $authDataStore")
                            if (authDataStore == null) {
                                Log.e("LoginViewModel", "authDataStore is null! No se puede guardar el usuario/tokens.")
                                _state.value = LoginState.Error("Error interno: almacenamiento no disponible.")
                                return@fold
                            }
                            // LOG: Token y expiración usando métodos públicos
                            val tokenFromCache = authDataStore.getApplicationToken()
                            val expirationFromCache = authDataStore.getTokenExpirationDate()
                            Log.d("LoginViewModel", "[DEBUG] Token from cache after login: $tokenFromCache")
                            Log.d("LoginViewModel", "[DEBUG] Token expiration from cache after login: $expirationFromCache")
                            if (tokenFromCache == null) {
                                Log.e("LoginViewModel", "Token from cache is null después de login.")
                            }
                            if (expirationFromCache == null) {
                                Log.e("LoginViewModel", "Token expiration from cache is null después de login.")
                            }
                            // Forzar recarga desde almacenamiento seguro
                            authDataStore.initializeApplicationToken()
                            val tokenAfterInit = authDataStore.getApplicationToken()
                            val expirationAfterInit = authDataStore.getTokenExpirationDate()
                            Log.d("LoginViewModel", "[DEBUG] Token after initializeApplicationToken: $tokenAfterInit")
                            Log.d("LoginViewModel", "[DEBUG] Token expiration after initializeApplicationToken: $expirationAfterInit")
                            // Validar campos críticos del modelo User
                            if (user.id.isNullOrBlank() || user.token.isNullOrBlank() || user.refreshToken.isNullOrBlank() || user.username.isNullOrBlank()) {
                                Log.e("LoginViewModel", "[LOGIN] User tiene campos críticos nulos o vacíos: id=${user.id}, token=${user.token}, refreshToken=${user.refreshToken}, username=${user.username}")
                            }
                            Log.d("LoginViewModel", "[LOGIN] Usuario y tokens guardados. Cargando contexto de negocio y preferencias...")
                            loadContextAndPreferences(user)
                        } catch (e: Exception) {
                            Log.e("LoginViewModel", "[LOGIN] Error guardando usuario/tokens: ${e.message}", e)
                            _state.value = LoginState.Error("Error interno al guardar sesión.")
                        }
                    },
                    onFailure = { exception ->
                        Log.e("LoginViewModel", "[LOGIN] Login failed", exception)
                        val userMessage = extractUserFriendlyError(exception.message)
                        _state.value = LoginState.Error(userMessage)
                    }
                )
            } catch (e: Exception) {
                Log.e("LoginViewModel", "[LOGIN] Login failed with exception", e)
                val userMessage = extractUserFriendlyError(e.message)
                _state.value = LoginState.Error(userMessage)
            }
        }
    }

    private suspend fun loadContextAndPreferences(user: User) {
        try {
            Log.d("LoginViewModel", "[CONTEXT] Loading business context after login...")
            businessContextManager.loadBusinessContext()
            Log.d("LoginViewModel", "[CONTEXT] Business context loaded successfully")
            Log.d("LoginViewModel", "[CONTEXT] Checking if user needs preferences setup...")
            val needsSetupFromPrefs = userPreferencesRepository.userNeedsPreferencesSetup()
            Log.d("LoginViewModel", "[CONTEXT] UserPreferences check completed: $needsSetupFromPrefs")
            val needsSetupFromContext = businessContextManager.userNeedsPreferencesSetup()
            Log.d("LoginViewModel", "[CONTEXT] BusinessContext check completed: $needsSetupFromContext")
            val needsSetup = needsSetupFromPrefs || needsSetupFromContext
            Log.d("LoginViewModel", "[CONTEXT] Preferences setup check summary: From UserPreferences: $needsSetupFromPrefs, From BusinessContext: $needsSetupFromContext, Final: $needsSetup")
            if (needsSetup) {
                Log.d("LoginViewModel", "[CONTEXT] User needs preferences setup, updating state to RequiresPreferencesSetup")
                _state.value = LoginState.RequiresPreferencesSetup(user)
            } else {
                Log.d("LoginViewModel", "[CONTEXT] User preferences are configured, updating state to Success")
                _state.value = LoginState.Success(user)
                // Ya no se guardan filtros aquí. Toda la persistencia de filtros es responsabilidad del ViewModel de filtros y FilterStorage.
            }
        } catch (e: Exception) {
            Log.w("LoginViewModel", "[CONTEXT] Exception loading business context or preferences: ${e.message}", e)
            _state.value = LoginState.RequiresPreferencesSetup(user)
        }
    }

    private fun extractUserFriendlyError(raw: String?): String {
        if (raw.isNullOrBlank()) return "Login failed. Please try again."
        // Busca mensajes comunes
        if (raw.contains("Unknown username or password", ignoreCase = true)) {
            return "Incorrect username or password."
        }
        if (raw.contains("401") || raw.contains("403")) {
            return "Could not authenticate. Please verify your credentials."
        }
        // Intenta extraer el campo 'title' del JSON si existe
        val titleRegex = """"title"\s*:\s*"([^"]+)""".toRegex()
        val match = titleRegex.find(raw)
        if (match != null) {
            return match.groupValues[1]
        }
        // Si no, muestra un mensaje genérico
        return "Login failed. Please try again."
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }
}

