package app.forku.core.auth

import android.content.Context
import android.util.Log
import app.forku.data.api.GOSecurityProviderApi
import app.forku.data.datastore.AuthDataStore
import app.forku.domain.repository.IGOSecurityProviderRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Provider

/**
 * Manages session keep-alive functionality to prevent session timeout
 * Uses GOSecurityProvider endpoints to maintain active session
 */
@Singleton
class SessionKeepAliveManager @Inject constructor(
    private val goSecurityApi: GOSecurityProviderApi,
    private val goSecurityProviderRepositoryProvider: Provider<IGOSecurityProviderRepository>,
    private val headerManager: HeaderManager,
    private val authDataStore: AuthDataStore,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SESSION_FLOW"
        
        // 🚀 PRODUCTION INTERVALS - Optimized for session retention
        private const val KEEP_ALIVE_INTERVAL_MS = 300_000L // 5 minutos
        private const val TOKEN_RENEWAL_INTERVAL_MS = 300_000L // 5 minutos
        
        // 🌙 BACKGROUND INTERVALS - More aggressive to prevent session loss
        private const val BACKGROUND_KEEP_ALIVE_INTERVAL_MS = 300_000L // 5 minutos
        private const val BACKGROUND_TOKEN_RENEWAL_INTERVAL_MS = 300_000L // 5 minutos
        
        // 🔋 DEEP BACKGROUND INTERVALS - CRITICAL FIX: Much more aggressive
        private const val DEEP_BACKGROUND_KEEP_ALIVE_INTERVAL_MS = 300_000L // 5 minutos
        private const val DEEP_BACKGROUND_TOKEN_RENEWAL_INTERVAL_MS = 300_000L // 5 minutos
        
        private const val INITIAL_DELAY_MS = 5_000L // 5 seconds
        private const val RETRY_DELAY_MS = 30_000L // 30 seconds (was 1 minute)
        
        // 🕒 SESSION EXPIRATION SETTINGS - Aligned with server requirements
        private const val MAX_BACKGROUND_DURATION_MS = 86_400_000L // 24 hours
        private const val DEEP_BACKGROUND_THRESHOLD_MS = 600_000L // 10 minutos (was 30) - enter deep background sooner
        private const val TOKEN_REFRESH_THRESHOLD_MS = 43_200_000L // 12 horas before expiration
        private const val TOKEN_CRITICAL_THRESHOLD_MS = 600_000L // 10 minutos
        
        // 🔄 Reduced retry intervals and increased tolerance
        private const val FAILED_REQUEST_RETRY_DELAY_MS = 120_000L // 2 minutes (was 5)
        private const val MAX_CONSECUTIVE_FAILURES = 5 // Allow more failures before giving up (was 3)
    }

    private val sessionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val requestMutex = Mutex() // 🔒 Synchronization for CSRF token usage
    
    private var keepAliveJob: Job? = null
    private var tokenRenewalJob: Job? = null

    private val _isKeepAliveActive = MutableStateFlow(false)
    val isKeepAliveActive: StateFlow<Boolean> = _isKeepAliveActive.asStateFlow()

    // 🌙 Background state management
    private val _isInBackground = MutableStateFlow(false)
    val isInBackground: StateFlow<Boolean> = _isInBackground.asStateFlow()
    
    private val _isInDeepBackground = MutableStateFlow(false)
    val isInDeepBackground: StateFlow<Boolean> = _isInDeepBackground.asStateFlow()
    
    private var backgroundStartTime: Long = System.currentTimeMillis() // ✅ Initialize with current time
    private var consecutiveFailures: Int = 0
    private var lastFailureTime: Long = 0
    
    // Dynamic intervals based on app state and background depth
    private val currentKeepAliveInterval: Long
        get() = when {
            _isInDeepBackground.value -> DEEP_BACKGROUND_KEEP_ALIVE_INTERVAL_MS
            _isInBackground.value -> BACKGROUND_KEEP_ALIVE_INTERVAL_MS
            else -> KEEP_ALIVE_INTERVAL_MS
        }
        
    private val currentTokenRenewalInterval: Long
        get() = when {
            _isInDeepBackground.value -> DEEP_BACKGROUND_TOKEN_RENEWAL_INTERVAL_MS
            _isInBackground.value -> BACKGROUND_TOKEN_RENEWAL_INTERVAL_MS
            else -> TOKEN_RENEWAL_INTERVAL_MS
        }

    private val _lastKeepAliveTime = MutableStateFlow<Long?>(null)
    val lastKeepAliveTime: StateFlow<Long?> = _lastKeepAliveTime.asStateFlow()

    private val _lastTokenRenewalAttempt = MutableStateFlow<Long?>(null)
    val lastTokenRenewalAttempt: StateFlow<Long?> = _lastTokenRenewalAttempt.asStateFlow()

    private val _tokenRenewalSuccessCount = MutableStateFlow(0)
    val tokenRenewalSuccessCount: StateFlow<Int> = _tokenRenewalSuccessCount.asStateFlow()

    private val _tokenRenewalFailureCount = MutableStateFlow(0)
    val tokenRenewalFailureCount: StateFlow<Int> = _tokenRenewalFailureCount.asStateFlow()

    // 🚨 NEW: Token expiration observable
    private val _tokenExpiration = MutableStateFlow<Date?>(null)
    val tokenExpiration: StateFlow<Date?> = _tokenExpiration.asStateFlow()

    private val _sessionExpiredEvent = MutableStateFlow(false)
    val sessionExpiredEvent: StateFlow<Boolean> = _sessionExpiredEvent.asStateFlow()

    // Nueva función para controlar y loguear cualquier cambio
    private fun setSessionExpiredEvent(value: Boolean, reason: String) {
        Log.e(TAG, "[SESSION_FLOW] [LOCK] setSessionExpiredEvent($value) called. Reason: $reason")
        Log.e(TAG, Log.getStackTraceString(Throwable()))
        if (_sessionExpiredEvent.value != value) {
            _sessionExpiredEvent.value = value
            Log.e(TAG, "[SESSION_FLOW] [LOCK] _sessionExpiredEvent.value set to $value (reason: $reason)")
        } else {
            Log.e(TAG, "[SESSION_FLOW] [LOCK] _sessionExpiredEvent.value already $value, skipping (reason: $reason)")
        }
    }

    // --- NUEVO: Método público para resetear el evento de sesión expirada ---
    fun resetSessionExpiredEvent(reason: String = "Manual reset after login") {
        setSessionExpiredEvent(false, reason)
    }

    // Agregar StateFlow para snackbar events
    private val _snackbarEvent = MutableStateFlow<String?>(null)
    val snackbarEvent: StateFlow<String?> = _snackbarEvent.asStateFlow()

    /**
     * Start session keep-alive service
     */
    fun startKeepAlive() {
        val token = authDataStore.getApplicationToken()
        if (token.isNullOrBlank()) {
            Log.w(TAG, "⛔ No valid application token found - not starting keep-alive service")
            _snackbarEvent.value = "No active session. Please log in."
            return
        }
        Log.d(TAG, "🚀 Starting session keep-alive service")
        
        if (_isKeepAliveActive.value) {
            Log.d(TAG, "⚠️ Keep-alive already active, stopping previous instance")
            stopKeepAlive()
        }
        
        _isKeepAliveActive.value = true
        Log.d(TAG, "✅ Keep-alive status set to active")

        // Start keep-alive pings
        keepAliveJob = sessionScope.launch {
            Log.d(TAG, "🔄 Keep-alive coroutine started")
            // 🧪 TESTING: Reduced initial delay for immediate testing
            delay(INITIAL_DELAY_MS) // 5 seconds (was KEEP_ALIVE_INTERVAL_MS)
            Log.d(TAG, "⏰ Initial delay completed, starting keep-alive loop")
            
            while (isActive) {
                try {
                    Log.d(TAG, "🔄 Keep-alive loop iteration starting")
                    if (!_isKeepAliveActive.value) {
                        Log.d(TAG, "🛑 Keep-alive deactivated, breaking loop")
                        break
                    }
                    
                    performKeepAlive()
                    // 🚨 NEW: Actualizar expiración tras keep-alive
                    _tokenExpiration.value = authDataStore.getTokenExpirationDate()
                    Log.d(TAG, "⏳ Waiting ${currentKeepAliveInterval}ms for next keep-alive")
                    delay(currentKeepAliveInterval)
                } catch (e: CancellationException) {
                    Log.d(TAG, "🔄 Keep-alive job cancelled - this is normal during app lifecycle changes")
                    throw e // Re-throw to properly cancel the coroutine
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error in keep-alive loop", e)
                    // Wait before retrying to avoid spam
                    delay(RETRY_DELAY_MS) // 30 seconds retry delay
                }
            }
            Log.d(TAG, "🏁 Keep-alive loop ended")
        }

        // Start token renewal with proactive checking
        tokenRenewalJob = sessionScope.launch {
            Log.d(TAG, "🔄 Token renewal coroutine started")
            while (isActive) {
                try {
                    Log.d(TAG, "⏳ Waiting ${currentTokenRenewalInterval}ms for token renewal check")
                    delay(currentTokenRenewalInterval)
                    
                    if (!_isKeepAliveActive.value) {
                        Log.d(TAG, "🛑 Keep-alive deactivated, breaking token renewal loop")
                        break
                    }
                    
                    Log.d(TAG, "🔄 Token renewal iteration starting - checking if renewal needed")
                    
                    // Only renew if token actually needs renewal
                    if (shouldRenewToken()) {
                        Log.d(TAG, "✅ Token renewal needed - proceeding with renewal")
                        performTokenRenewal()
                        // 🚨 NEW: Actualizar expiración tras renovación
                        _tokenExpiration.value = authDataStore.getTokenExpirationDate()
                    } else {
                        Log.d(TAG, "⏭️ Token renewal not needed - skipping this cycle")
                    }
                } catch (e: CancellationException) {
                    Log.d(TAG, "🔄 Token renewal job cancelled - this is normal during app lifecycle changes")
                    throw e // Re-throw to properly cancel the coroutine
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error in token renewal loop", e)
                    delay(RETRY_DELAY_MS)
                }
            }
            Log.d(TAG, "🏁 Token renewal loop ended")
        }
        
        Log.d(TAG, "🎯 Session keep-alive service fully started")
    }

    /**
     * Stop session keep-alive service
     */
    fun stopKeepAlive() {
        Log.d(TAG, "🛑 Stopping session keep-alive service")
        
        keepAliveJob?.let { job ->
            Log.d(TAG, "🔄 Cancelling keep-alive job (isActive: ${job.isActive})")
            job.cancel()
        } ?: Log.d(TAG, "⚠️ Keep-alive job was null")
        
        tokenRenewalJob?.let { job ->
            Log.d(TAG, "🔄 Cancelling token renewal job (isActive: ${job.isActive})")
            job.cancel()
        } ?: Log.d(TAG, "⚠️ Token renewal job was null")
        
        keepAliveJob = null
        tokenRenewalJob = null
        
        _isKeepAliveActive.value = false
        _lastKeepAliveTime.value = null
        _lastTokenRenewalAttempt.value = null
        _tokenRenewalSuccessCount.value = 0
        _tokenRenewalFailureCount.value = 0
        
        Log.d(TAG, "✅ Session keep-alive service stopped and cleaned up")
    }

    /**
     * Perform keep-alive ping to maintain session
     */
    suspend fun performKeepAlive(): Boolean {
        return requestMutex.withLock {
            try {
                // LOG: Token y expiración en cada check
                val token = authDataStore.getApplicationToken()
                val expiration = authDataStore.getTokenExpirationDate()
                Log.d(TAG, "[DEBUG] SessionKeepAliveManager - Token: $token")
                Log.d(TAG, "[DEBUG] SessionKeepAliveManager - Token expiration: $expiration")
                // Check if we should skip due to consecutive failures
                if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                    val timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime
                    if (timeSinceLastFailure < FAILED_REQUEST_RETRY_DELAY_MS) {
                        Log.d(TAG, "⏭️ Skipping keep-alive due to consecutive failures (${consecutiveFailures})")
                        return false
                    } else {
                        Log.d(TAG, "🔄 Retry timeout reached, attempting keep-alive after failures")
                        consecutiveFailures = 0 // Reset counter
                    }
                }
                
                Log.d(TAG, "🔄 Performing keep-alive ping...")
                
                val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
                Log.d(TAG, "🔍 Keep-alive headers obtained:")
                Log.d(TAG, "  - CSRF Token: ${csrfToken.take(20)}...")
                Log.d(TAG, "  - Cookie length: ${cookie.length}")
                
                val response = goSecurityApi.keepAlive(csrfToken, cookie)
                
                if (response.isSuccessful) {
                    _lastKeepAliveTime.value = System.currentTimeMillis()
                    consecutiveFailures = 0 // Reset failure counter on success
                    Log.d(TAG, "✅ Keep-alive successful")
                    
                    // Check if we should transition to deep background mode
                    checkForDeepBackgroundTransition()
                    
                    // 🚨 NEW: Actualizar expiración tras keep-alive
                    _tokenExpiration.value = authDataStore.getTokenExpirationDate()
                    
                    true
                } else {
                    consecutiveFailures++
                    lastFailureTime = System.currentTimeMillis()
                    Log.w(TAG, "⚠️ Keep-alive failed: ${response.code()} (failures: $consecutiveFailures)")
                    val errorBody = response.errorBody()?.string()
                    Log.w(TAG, "⚠️ Keep-alive error body: $errorBody")
                    false
                }
            } catch (e: Exception) {
                consecutiveFailures++
                lastFailureTime = System.currentTimeMillis()
                Log.e(TAG, "❌ Keep-alive error (failures: $consecutiveFailures)", e)
                false
            }
        }
    }

    /**
     * Check if token needs renewal based on expiration time
     */
    suspend fun shouldRenewToken(): Boolean {
        val tokenExpiration = authDataStore.getTokenExpirationDate()
        Log.d(TAG, "[SESSION_FLOW] [LOCK] shouldRenewToken() called")
        Log.d(TAG, "[SESSION_FLOW] [LOCK] shouldRenewToken Token actual: ${authDataStore.getApplicationToken()?.take(30)}...")
        Log.d(TAG, "[SESSION_FLOW] [LOCK] shouldRenewToken Token expiration date: $tokenExpiration")
        if (tokenExpiration == null) {
            Log.w(TAG, "⚠️ No token expiration date available - assuming renewal needed")
            _snackbarEvent.value = "Token expiration unknown. Renewal forced." // Snackbar
            return true
        }
        val now = System.currentTimeMillis()
        val timeUntilExpiration = tokenExpiration.time - now
        Log.d(TAG, "[SESSION_FLOW] [LOCK] Token expiration: $tokenExpiration (epoch=${tokenExpiration.time}), now: $now, timeUntilExpiration: $timeUntilExpiration ms")
        Log.d(TAG, "[SESSION_FLOW] [LOCK] TOKEN_CRITICAL_THRESHOLD_MS: $TOKEN_CRITICAL_THRESHOLD_MS, TOKEN_REFRESH_THRESHOLD_MS: $TOKEN_REFRESH_THRESHOLD_MS")
        when {
            timeUntilExpiration <= 0 -> {
                Log.e(TAG, "⚠️ Token already expired (${-timeUntilExpiration / 1000}s ago) - immediate renewal required")
                _snackbarEvent.value = "Token expired. Immediate renewal required." // Snackbar
                return true
            }
            timeUntilExpiration <= TOKEN_CRITICAL_THRESHOLD_MS -> {
                Log.w(TAG, "⚠️ Token expires very soon ($timeUntilExpiration ms left) - critical renewal needed")
                _snackbarEvent.value = "Token about to expire. Renewal needed." // Snackbar
                return true
            }
            timeUntilExpiration <= TOKEN_REFRESH_THRESHOLD_MS -> {
                Log.i(TAG, "⚠️ Token expires soon ($timeUntilExpiration ms left) - renewal recommended")
                _snackbarEvent.value = "Token expires soon. Renewal recommended." // Snackbar
                return true
            }
            else -> {
                Log.d(TAG, "⚠️ Token still valid for $timeUntilExpiration ms - no renewal needed")
                return false
            }
        }
    }

    /**
     * Handle expired token scenario - stop keep-alive and notify
     */
    private fun handleExpiredToken(reason: String = "Unknown") {
        Log.e(TAG, "[SESSION_FLOW] [LOCK] handleExpiredToken() called. StackTrace:")
        Log.e(TAG, Log.getStackTraceString(Throwable()))
        Log.e(TAG, "[SESSION_FLOW] [LOCK] handleExpiredToken Token actual: ${authDataStore.getApplicationToken()?.take(30)}...")
        Log.e(TAG, "[SESSION_FLOW] [LOCK] handleExpiredToken Token expiration: ${authDataStore.getTokenExpirationDate()}")
        Log.e(TAG, "[SESSION_FLOW] [LOCK] handleExpiredToken Expired reason: $reason")
        stopKeepAlive()
        setSessionExpiredEvent(true, reason)
    }

    /**
     * Perform token renewal to extend session
     */
    suspend fun performTokenRenewal(): Boolean {
        return requestMutex.withLock {
            try {
                Log.d(TAG, "🔄 Performing token renewal...")
                _lastTokenRenewalAttempt.value = System.currentTimeMillis()
                
                // Check if we have an authentication token (refresh token) for renewal
                val authToken = authDataStore.getAuthenticationToken()
                if (authToken == null) {
                    Log.e(TAG, "💀 No authentication token found - cannot renew session")
                    handleExpiredToken("No authentication token found - cannot renew session")
                    return@withLock false
                }
                
                // Check if token is already expired before attempting renewal
                val tokenExpiration = authDataStore.getTokenExpirationDate()
                if (tokenExpiration != null) {
                    val timeUntilExpiration = tokenExpiration.time - System.currentTimeMillis()
                    if (timeUntilExpiration <= -60_000L) { // More than 1 minute expired
                        Log.e(TAG, "💀 Token expired too long ago (${-timeUntilExpiration / 1000}s) - cannot renew")
                        handleExpiredToken("Token expired too long ago - cannot renew")
                        return@withLock false
                    }
                }
                
                // 🔧 FIX: Use GOSecurityProviderRepository for proper token renewal
                Log.d(TAG, "🔄 Using GOSecurityProviderRepository for token renewal...")
                val goSecurityProviderRepository = goSecurityProviderRepositoryProvider.get()
                val renewalResult = goSecurityProviderRepository.renewToken()
                
                if (renewalResult.isSuccess) {
                    val newUser = renewalResult.getOrNull()
                    if (newUser != null) {
                        // PRESERVE CONTEXT: Get current user and merge context fields if needed
                        val currentUser = authDataStore.getCurrentUser()
                        val mergedUser = newUser.copy(
                            businessId = newUser.businessId ?: currentUser?.businessId,
                            siteId = newUser.siteId ?: currentUser?.siteId,
                            systemOwnerId = newUser.systemOwnerId ?: currentUser?.systemOwnerId,
                            userPreferencesId = newUser.userPreferencesId ?: currentUser?.userPreferencesId
                        )
                        Log.d(TAG, "[TOKEN RENEWAL] Before renewal: businessId=${currentUser?.businessId}, siteId=${currentUser?.siteId}, systemOwnerId=${currentUser?.systemOwnerId}, userPreferencesId=${currentUser?.userPreferencesId}")
                        Log.d(TAG, "[TOKEN RENEWAL] After renewal: businessId=${mergedUser.businessId}, siteId=${mergedUser.siteId}, systemOwnerId=${mergedUser.systemOwnerId}, userPreferencesId=${mergedUser.userPreferencesId}")
                        // Update the stored user and tokens
                        authDataStore.setCurrentUser(mergedUser)
                        _tokenRenewalSuccessCount.value = _tokenRenewalSuccessCount.value + 1
                        Log.d(TAG, "✅ Token renewal completed successfully (${_tokenRenewalSuccessCount.value} successes)")
                        // Force reinitialize tokens to ensure cache is updated
                        authDataStore.initializeApplicationToken()
                        // 🚨 NEW: Actualizar expiración tras renovación
                        _tokenExpiration.value = authDataStore.getTokenExpirationDate()
                        // Show snackbar/notification (handled via callback or event)
                        _snackbarEvent.value = "Token renewed successfully. Context preserved."
                        true
                    } else {
                        Log.e(TAG, "❌ Token renewal successful but no user returned")
                        _tokenRenewalFailureCount.value = _tokenRenewalFailureCount.value + 1
                        false
                    }
                } else {
                    _tokenRenewalFailureCount.value = _tokenRenewalFailureCount.value + 1
                    val error = renewalResult.exceptionOrNull()
                    Log.w(TAG, "⚠️ Token renewal failed (${_tokenRenewalFailureCount.value} failures): ${error?.message}")
                    
                    // Enhanced error classification for better handling
                    val isServerError = error?.message?.contains("500") == true || 
                                       error?.message?.contains("Server error") == true
                    val isNetworkError = error?.message?.contains("timeout") == true ||
                                        error?.message?.contains("network") == true ||
                                        error?.message?.contains("ConnectException") == true
                    val isAuthError = error?.message?.contains("SecurityTokenExpiredException") == true || 
                                     error?.message?.contains("401") == true ||
                                     error?.message?.contains("403") == true ||
                                     error?.message?.contains("Authentication failed") == true
                    
                    when {
                        isAuthError -> {
                            Log.e(TAG, "💀 Token renewal failed with authentication error - session expired")
                            handleExpiredToken("Token renewal failed with authentication error - session expired")
                            return@withLock false
                        }
                        
                        isServerError -> {
                            Log.w(TAG, "🚨 Server error (500) during token renewal - this is likely temporary")
                            Log.w(TAG, "💡 Current token may still be valid, continuing with session keep-alive")
                            Log.w(TAG, "🔄 Will retry token renewal in next cycle")
                            
                            // For server errors, don't kill the session immediately
                            // The current token might still be valid
                            if (_tokenRenewalFailureCount.value >= 5) {
                                Log.e(TAG, "💀 Too many consecutive server errors (${_tokenRenewalFailureCount.value}) - session may be unstable")
                                Log.w(TAG, "🔄 Will continue trying but session health is degraded")
                            }
                            false // Continue keep-alive, don't expire session
                        }
                        
                        isNetworkError -> {
                            Log.w(TAG, "🌐 Network error during token renewal - continuing with session")
                            Log.w(TAG, "💡 This is likely a connectivity issue, not an authentication problem")
                            
                            if (_tokenRenewalFailureCount.value >= 7) {
                                Log.w(TAG, "🔄 Multiple network errors, but continuing session keep-alive")
                            }
                            false // Continue keep-alive
                        }
                        
                        else -> {
                            Log.w(TAG, "❓ Unknown error type during token renewal")
                            Log.w(TAG, "🔄 Treating as temporary issue, continuing with session")
                            
                            // For unknown errors, be more conservative
                            if (_tokenRenewalFailureCount.value >= 3) {
                                Log.e(TAG, "💀 Multiple unknown errors - session may be compromised")
                                handleExpiredToken("Multiple unknown errors - session may be compromised")
                                return@withLock false
                            }
                            false
                        }
                    }
                }
            } catch (e: Exception) {
                _tokenRenewalFailureCount.value = _tokenRenewalFailureCount.value + 1
                Log.e(TAG, "❌ Token renewal error (${_tokenRenewalFailureCount.value} failures)", e)
                false
            }
        }
    }

    /**
     * Manually trigger keep-alive (useful for user activity)
     */
    fun triggerKeepAlive() {
        if (!_isKeepAliveActive.value) return
        
        sessionScope.launch {
            performKeepAlive()
        }
    }

    /**
     * Manually trigger token renewal check (useful for critical operations)
     */
    fun triggerTokenRenewalCheck() {
        if (!_isKeepAliveActive.value) return
        
        sessionScope.launch {
            if (shouldRenewToken()) {
                Log.d(TAG, "🔄 Manual token renewal check - renewal needed")
                performTokenRenewal()
            } else {
                Log.d(TAG, "✅ Manual token renewal check - no renewal needed")
            }
        }
    }

    /**
     * Get time since last successful keep-alive
     */
    fun getTimeSinceLastKeepAlive(): Long? {
        val lastTime = _lastKeepAliveTime.value ?: return null
        return System.currentTimeMillis() - lastTime
    }

    /**
     * Check if session might be expired based on keep-alive status
     */
    fun isSessionLikelyExpired(): Boolean {
        val timeSince = getTimeSinceLastKeepAlive() ?: return true
        val threshold = when {
            _isInDeepBackground.value -> DEEP_BACKGROUND_KEEP_ALIVE_INTERVAL_MS * 2
            _isInBackground.value -> BACKGROUND_KEEP_ALIVE_INTERVAL_MS * 2
            else -> KEEP_ALIVE_INTERVAL_MS * 2
        }
        return timeSince > threshold
    }

    /**
     * ENHANCED: Force immediate session health check - Use when critical operations are about to be performed
     */
    suspend fun performEmergencySessionCheck(): Boolean {
        Log.w(TAG, "🚨 Emergency session check triggered - forcing immediate keep-alive and token validation")
        
        // First check if token is critically expired
        if (shouldRenewToken()) {
            Log.w(TAG, "🔄 Token needs renewal during emergency check")
            val renewalSuccess = performTokenRenewal()
            if (!renewalSuccess) {
                Log.e(TAG, "💀 Token renewal failed during emergency check - session likely dead")
                return false
            }
        }
        
        // Perform immediate keep-alive
        val keepAliveSuccess = performKeepAlive()
        if (keepAliveSuccess) {
            Log.i(TAG, "✅ Emergency session check passed - session is healthy")
            // Reset failure counter on successful emergency check
            consecutiveFailures = 0
        } else {
            Log.e(TAG, "💀 Emergency session check failed - session appears dead")
        }
        
        return keepAliveSuccess
    }

    /**
     * Enhanced session health statistics with critical warnings
     */
    fun getSessionHealthStats(): String {
        val currentTime = System.currentTimeMillis()
        val lastKeepAlive = _lastKeepAliveTime.value?.let { 
            val timeSince = (currentTime - it) / 1000
            "Last keep-alive: ${timeSince}s ago"
        } ?: "No keep-alive yet"
        
        val lastTokenRenewal = _lastTokenRenewalAttempt.value?.let {
            val timeSince = (currentTime - it) / 1000
            "Last token renewal attempt: ${timeSince}s ago"
        } ?: "No token renewal yet"
        
        val successRate = if (_tokenRenewalSuccessCount.value + _tokenRenewalFailureCount.value > 0) {
            val total = _tokenRenewalSuccessCount.value + _tokenRenewalFailureCount.value
            val percentage = (_tokenRenewalSuccessCount.value * 100) / total
            "$percentage% success rate"
        } else {
            "No attempts yet"
        }
        
        // Add critical warnings
        val warnings = mutableListOf<String>()
        if (isSessionLikelyExpired()) {
            warnings.add("⚠️ SESSION LIKELY EXPIRED")
        }
        if (consecutiveFailures >= 3) {
            warnings.add("⚠️ MULTIPLE CONSECUTIVE FAILURES ($consecutiveFailures)")
        }
        if (_tokenRenewalFailureCount.value > _tokenRenewalSuccessCount.value && _tokenRenewalFailureCount.value > 2) {
            warnings.add("⚠️ TOKEN RENEWAL PROBLEMS")
        }
        
        val warningText = if (warnings.isNotEmpty()) {
            "\n🚨 WARNINGS:\n${warnings.joinToString("\n")}\n"
        } else {
            "\n✅ No critical warnings\n"
        }
        
        return """
            📊 Session Keep-Alive Status:
            - Active: ${_isKeepAliveActive.value}
            - Background mode: ${when {
                _isInDeepBackground.value -> "Deep Background"
                _isInBackground.value -> "Background"
                else -> "Foreground"
            }}
            - Keep-alive job active: ${keepAliveJob?.isActive ?: false}
            - Token renewal job active: ${tokenRenewalJob?.isActive ?: false}
            - $lastKeepAlive
            - $lastTokenRenewal
            - Token renewal successes: ${_tokenRenewalSuccessCount.value}
            - Token renewal failures: ${_tokenRenewalFailureCount.value}
            - Consecutive failures: $consecutiveFailures
            - $successRate
            - Current keep-alive interval: ${currentKeepAliveInterval / 1000}s
            - Current token renewal interval: ${currentTokenRenewalInterval / 1000}s$warningText
        """.trimIndent()
    }

    /**
     * Log current session status for debugging
     */
    fun logSessionStatus() {
        Log.i(TAG, getSessionHealthStats())
    }

    /**
     * Force immediate session debugging - logs detailed status and performs health check
     */
    suspend fun debugSession(): String {
        val debugInfo = StringBuilder()
        debugInfo.append("🔍 SESSION DEBUG REPORT\n")
        debugInfo.append("=".repeat(50)).append("\n")
        debugInfo.append(getSessionHealthStats()).append("\n")
        debugInfo.append("=".repeat(50)).append("\n")
        
        // Test session health
        debugInfo.append("🧪 TESTING SESSION HEALTH...\n")
        val emergencyCheckResult = performEmergencySessionCheck()
        debugInfo.append("Emergency check result: ${if (emergencyCheckResult) "✅ PASSED" else "❌ FAILED"}\n")
        
        // Log token expiration info
        try {
            val tokenExp = authDataStore.getTokenExpirationDate()
            if (tokenExp != null) {
                val timeUntilExp = tokenExp.time - System.currentTimeMillis()
                debugInfo.append("Token expires in: ${timeUntilExp / 1000}s (${timeUntilExp / 60000}min)\n")
            } else {
                debugInfo.append("Token expiration: Unknown\n")
            }
        } catch (e: Exception) {
            debugInfo.append("Token expiration check failed: ${e.message}\n")
        }
        
        val reportText = debugInfo.toString()
        Log.w(TAG, reportText)
        return reportText
    }

    /**
     * 🚀 FOR TESTING: Force immediate execution of keep-alive and token renewal
     * Uses separate coroutines to avoid mutex deadlock
     */
    suspend fun forceExecuteNow() {
        if (!_isKeepAliveActive.value) {
            Log.w(TAG, "⚠️ Cannot force execute - service is not active")
            return
        }
        
        Log.d(TAG, "🚀 FORCING IMMEDIATE EXECUTION FOR TESTING")
        
        // Execute keep-alive and token renewal in separate coroutines to avoid mutex deadlock
        val keepAliveJob = sessionScope.async {
            Log.d(TAG, "🔄 Executing immediate keep-alive...")
            try {
                val result = performKeepAlive()
                Log.d(TAG, "✅ Immediate keep-alive completed: $result")
                result
            } catch (e: Exception) {
                Log.e(TAG, "❌ Immediate keep-alive failed", e)
                false
            }
        }
        
        val tokenRenewalJob = sessionScope.async {
            // Add small delay to avoid immediate mutex conflict
            delay(1000)
            Log.d(TAG, "🔄 Executing immediate token renewal...")
            try {
                val result = performTokenRenewal()
                Log.d(TAG, "✅ Immediate token renewal completed: $result")
                result
            } catch (e: Exception) {
                Log.e(TAG, "❌ Immediate token renewal failed", e)
                false
            }
        }
        
        // Wait for both to complete
        try {
            val keepAliveResult = keepAliveJob.await()
            val tokenRenewalResult = tokenRenewalJob.await()
            
            Log.d(TAG, "🎯 Immediate execution completed - Keep-alive: $keepAliveResult, Token renewal: $tokenRenewalResult")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error waiting for immediate execution", e)
        }
        
        logSessionStatus()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopKeepAlive()
        sessionScope.cancel()
    }
    
    /**
     * 🌙 Handle app going to background
     */
    fun onAppGoesToBackground() {
        Log.d(TAG, "🌙 App going to background - switching to background intervals")
        _isInBackground.value = true
        _isInDeepBackground.value = false // Reset deep background state
        backgroundStartTime = System.currentTimeMillis()
        consecutiveFailures = 0 // Reset failure counter on background transition
        
        // Restart jobs with new intervals if keep-alive is active
        if (_isKeepAliveActive.value) {
            Log.d(TAG, "🔄 Restarting jobs with background intervals")
            restartJobsWithCurrentIntervals()
        }
    }
    
    /**
     * 🌅 Handle app coming to foreground
     */
    suspend fun onAppComesToForeground(): Boolean {
        Log.d(TAG, "🌅 App coming to foreground - checking session validity")
        val wasInDeepBackground = _isInDeepBackground.value
        _isInBackground.value = false
        _isInDeepBackground.value = false
        consecutiveFailures = 0 // Reset failure counter on foreground return
        
        // ✅ FIXED: Ensure backgroundStartTime is valid to prevent calculation errors
        if (backgroundStartTime == 0L) {
            Log.w(TAG, "⚠️ backgroundStartTime was 0 - initializing to current time")
            backgroundStartTime = System.currentTimeMillis()
        }
        
        val backgroundDuration = System.currentTimeMillis() - backgroundStartTime
        Log.d(TAG, "📊 Background duration: ${backgroundDuration / 1000}s (was in deep background: $wasInDeepBackground)")
        Log.d(TAG, "📊 Debug: backgroundStartTime=${backgroundStartTime}, currentTime=${System.currentTimeMillis()}")
        
        // Check if session is still valid
        val isSessionValid = validateSessionAfterBackground(backgroundDuration)
        
        if (isSessionValid && _isKeepAliveActive.value) {
            Log.d(TAG, "✅ Session valid - switching to foreground intervals")
            restartJobsWithCurrentIntervals()
            
            // Perform immediate keep-alive and token renewal to refresh session
            sessionScope.launch {
                Log.d(TAG, "🔄 Performing immediate session refresh after background")
                performKeepAlive()
                delay(1000)
                performTokenRenewal()
            }
        } else if (!isSessionValid) {
            Log.w(TAG, "❌ Session expired during background - stopping keep-alive")
            stopKeepAlive()
        }
        
        return isSessionValid
    }
    
    /**
     * 🔍 Validate if session is still valid after background period
     */
    private suspend fun validateSessionAfterBackground(backgroundDuration: Long): Boolean {
        // ✅ FIXED: Validate background duration to prevent false session expiration
        if (backgroundDuration < 0) {
            Log.w(TAG, "⚠️ Invalid background duration: ${backgroundDuration}ms - resetting to 0")
            return true // Don't fail session due to calculation error
        }
        
        if (backgroundDuration > MAX_BACKGROUND_DURATION_MS) {
            Log.w(TAG, "⏰ Background duration exceeded maximum (${backgroundDuration / 1000}s > ${MAX_BACKGROUND_DURATION_MS / 1000}s)")
            // ✅ ADDITIONAL CHECK: If duration is absurdly high, it's likely a calculation error
            if (backgroundDuration > MAX_BACKGROUND_DURATION_MS * 10) {
                Log.e(TAG, "🚨 Background duration suspiciously high (${backgroundDuration / 1000}s) - likely calculation error, ignoring")
                return true // Don't fail session due to calculation error
            }
            return false
        }
        
        // Check if token needs renewal
        if (shouldRenewToken()) {
            Log.w(TAG, "⏰ Token needs renewal after background - attempting refresh")
            // Try to refresh token before considering session invalid
            val renewalSuccess = performTokenRenewal()
            if (!renewalSuccess) {
                Log.e(TAG, "❌ Token renewal failed after background - session likely invalid")
                return false
            }
        }
        
        // Try a quick keep-alive to validate session
        return try {
            Log.d(TAG, "🔍 Testing session validity with keep-alive")
            performKeepAlive()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Session validation failed", e)
            false
        }
    }
    
    /**
     * 🔄 Restart jobs with current intervals (foreground/background)
     */
    private fun restartJobsWithCurrentIntervals() {
        // Cancel existing jobs
        keepAliveJob?.cancel()
        tokenRenewalJob?.cancel()
        
        val intervalType = if (_isInBackground.value) "background" else "foreground"
        Log.d(TAG, "🔄 Starting $intervalType jobs - Keep-alive: ${currentKeepAliveInterval / 1000}s, Token renewal: ${currentTokenRenewalInterval / 1000}s")
        
        // Start keep-alive job with current interval
        keepAliveJob = sessionScope.launch {
            delay(INITIAL_DELAY_MS)
            while (isActive && _isKeepAliveActive.value) {
                try {
                    performKeepAlive()
                    delay(currentKeepAliveInterval)
                } catch (e: CancellationException) {
                    Log.d(TAG, "🔄 Keep-alive job cancelled - this is normal during app lifecycle changes")
                    throw e // Re-throw to properly cancel the coroutine
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error in keep-alive loop", e)
                    delay(RETRY_DELAY_MS)
                }
            }
        }
        
        // Start token renewal job with current interval
        tokenRenewalJob = sessionScope.launch {
            while (isActive && _isKeepAliveActive.value) {
                try {
                    delay(currentTokenRenewalInterval)
                    performTokenRenewal()
                } catch (e: CancellationException) {
                    Log.d(TAG, "🔄 Token renewal job cancelled - this is normal during app lifecycle changes")
                    throw e // Re-throw to properly cancel the coroutine
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error in token renewal loop", e)
                    delay(RETRY_DELAY_MS)
                }
            }
        }
    }

    /**
     * Build cookie string with ApplicationToken (same logic as AuthInterceptor)
     */
    private fun buildCookieString(
        applicationToken: String?,
        antiforgeryCookie: String?
    ): String {
        val cookies = mutableListOf<String>()
        
        // Add ApplicationToken and BearerToken
        applicationToken?.let {
            cookies.add("ApplicationToken=$it")
            cookies.add("BearerToken=$it")
            Log.d(TAG, "🔍 Added ApplicationToken and BearerToken to cookie")
        } ?: Log.d(TAG, "🔍 No ApplicationToken available")
        
        // Add Antiforgery cookie
        antiforgeryCookie?.let {
            if (!it.contains(";") && !it.contains(",")) {
                cookies.add(it)
                Log.d(TAG, "🔍 Added Antiforgery cookie: ${it.take(50)}...")
            } else {
                Log.w(TAG, "Invalid Antiforgery cookie format: $it")
                val firstPart = it.split(";").firstOrNull()?.split(",")?.firstOrNull()
                firstPart?.let { validPart -> 
                    cookies.add(validPart)
                    Log.d(TAG, "🔍 Added cleaned Antiforgery cookie: ${validPart.take(50)}...")
                }
            }
        } ?: Log.d(TAG, "🔍 No Antiforgery cookie available")
        
        val result = cookies.joinToString("; ")
        Log.d(TAG, "🔍 Final cookie string length: ${result.length}")
        return result
    }

    /**
     * 🔍 Check if we should transition to deep background mode
     */
    private fun checkForDeepBackgroundTransition() {
        if (_isInBackground.value && !_isInDeepBackground.value) {
            val backgroundDuration = System.currentTimeMillis() - backgroundStartTime
            if (backgroundDuration >= DEEP_BACKGROUND_THRESHOLD_MS) {
                Log.d(TAG, "🏔️ Transitioning to deep background mode after ${backgroundDuration / 1000}s")
                _isInDeepBackground.value = true
                
                // Restart jobs with deep background intervals
                if (_isKeepAliveActive.value) {
                    restartJobsWithCurrentIntervals()
                }
            }
        }
    }

    /**
     * Forzar actualización del observable de expiración de token (para uso desde la UI)
     */
    fun forceTokenExpirationRefresh() {
        _tokenExpiration.value = authDataStore.getTokenExpirationDate()
    }

    // Agregar función pública para limpiar el evento de snackbar
    fun clearSnackbarEvent() {
        _snackbarEvent.value = null
    }

    // Agregar función pública para mostrar snackbar
    fun showSnackbar(message: String) {
        _snackbarEvent.value = message
    }
} 