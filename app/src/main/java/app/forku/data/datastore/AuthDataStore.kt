package app.forku.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.forku.data.service.GOServicesManager
import android.util.Base64
import org.json.JSONObject
import java.util.Date
import android.util.Log

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

@Singleton
class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val USER_KEY = stringPreferencesKey("user")
        
        // Token keys with clear naming
        val APPLICATION_TOKEN = stringPreferencesKey("application_token")
        val AUTHENTICATION_TOKEN = stringPreferencesKey("authentication_token")
        val CSRF_TOKEN = stringPreferencesKey("csrf_token")
        val ANTIFORGERY_COOKIE = stringPreferencesKey("antiforgery_cookie")
        
        // User properties
        val USER_ID = stringPreferencesKey("userId")
        val EMAIL = stringPreferencesKey("email")
        val USERNAME = stringPreferencesKey("username")
        val FIRST_NAME = stringPreferencesKey("firstName")
        val LAST_NAME = stringPreferencesKey("lastName")
        val PHOTO_URL = stringPreferencesKey("photoUrl")
        val ROLE = stringPreferencesKey("role")
        val PASSWORD = stringPreferencesKey("password")
        val LAST_ACTIVE = stringPreferencesKey("lastActive")
        val IS_ONLINE = booleanPreferencesKey("isOnline")
        val BUSINESS_ID = stringPreferencesKey("businessId")
        val SITE_ID = stringPreferencesKey("siteId")
        val SYSTEM_OWNER_ID = stringPreferencesKey("systemOwnerId")
    }

    private var cachedApplicationToken: String? = null
    private var cachedAuthenticationToken: String? = null
    private var cachedCsrfToken: String? = null
    private var cachedAntiforgeryCookie: String? = null
    private var lastActiveTime: Long = 0
    private val gson = Gson()

    // --- Secure token storage using EncryptedSharedPreferences (added for security) ---
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    private val securePrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "auth_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    // --- End secure token storage ---

    // Application Token methods (main JWT token with user claims)
    val applicationToken: Flow<String?> = kotlinx.coroutines.flow.flow {
        emit(securePrefs.getString("application_token", null))
    }

    suspend fun initializeApplicationToken() {
        android.util.Log.d("AuthDataStore", "Initializing application token...")
        cachedApplicationToken = securePrefs.getString("application_token", null)
        cachedAuthenticationToken = securePrefs.getString("authentication_token", null)
        cachedCsrfToken = securePrefs.getString("csrf_token", null)
        cachedAntiforgeryCookie = securePrefs.getString("antiforgery_cookie", null)
        android.util.Log.d("AuthDataStore", "Application token initialized: ${cachedApplicationToken?.take(10) ?: "null"}")
        android.util.Log.d("AuthDataStore", "Authentication token initialized: ${cachedAuthenticationToken?.take(10) ?: "null"}")
        android.util.Log.d("AuthDataStore", "CSRF token initialized: ${cachedCsrfToken?.take(10) ?: "null"}")
        android.util.Log.d("AuthDataStore", "Antiforgery cookie initialized: ${cachedAntiforgeryCookie?.take(20) ?: "null"}")
    }
    
    fun getApplicationToken(): String? {
        // Always try to reload from secure storage if cache is null
        if (cachedApplicationToken == null) {
            cachedApplicationToken = securePrefs.getString("application_token", null)
            android.util.Log.d("AuthDataStore", "Reloaded application token from storage: ${cachedApplicationToken?.take(10) ?: "null"}")
        }
        android.util.Log.d("AuthDataStore", "Getting cached application token: ${cachedApplicationToken?.take(10) ?: "null"}")
        return cachedApplicationToken
    }
    
    suspend fun saveApplicationToken(token: String) {
        securePrefs.edit().putString("application_token", token).apply()
        cachedApplicationToken = token
        android.util.Log.d("AuthDataStore", "Saved application token: "+token.take(10)+"...")
    }

    // Authentication Token methods (refresh token)
    suspend fun saveAuthenticationToken(token: String) {
        securePrefs.edit().putString("authentication_token", token).apply()
        cachedAuthenticationToken = token
        android.util.Log.d("AuthDataStore", "Saved authentication token: "+token.take(10)+"...")
    }
    
    fun getAuthenticationToken(): String? {
        if (cachedAuthenticationToken == null) {
            cachedAuthenticationToken = securePrefs.getString("authentication_token", null)
            android.util.Log.d("AuthDataStore", "Reloaded authentication token from storage: ${cachedAuthenticationToken?.take(10) ?: "null"}")
        }
        return cachedAuthenticationToken
    }

    // CSRF Token methods
    suspend fun saveCsrfToken(token: String) {
        securePrefs.edit().putString("csrf_token", token).apply()
        cachedCsrfToken = token
        android.util.Log.d("AuthDataStore", "Saved CSRF token: ${token.take(10)}...")
    }
    
    fun getCsrfToken(): String? {
        if (cachedCsrfToken == null) {
            cachedCsrfToken = securePrefs.getString("csrf_token", null)
            android.util.Log.d("AuthDataStore", "Reloaded CSRF token from storage: ${cachedCsrfToken?.take(10) ?: "null"}")
        }
        return cachedCsrfToken
    }

    // Antiforgery Cookie methods
    suspend fun saveAntiforgeryCookie(cookie: String) {
        securePrefs.edit().putString("antiforgery_cookie", cookie).apply()
        cachedAntiforgeryCookie = cookie
        android.util.Log.d("AuthDataStore", "Saved Antiforgery cookie: ${cookie.take(20)}...")
    }
    
    fun getAntiforgeryCookie(): String? {
        if (cachedAntiforgeryCookie == null) {
            cachedAntiforgeryCookie = securePrefs.getString("antiforgery_cookie", null)
            android.util.Log.d("AuthDataStore", "Reloaded antiforgery cookie from storage: ${cachedAntiforgeryCookie?.take(20) ?: "null"}")
        }
        return cachedAntiforgeryCookie
    }

    // Suspend, for ViewModels/repos (loads from DataStore if needed)
    suspend fun getCsrfTokenSuspend(): String? {
        if (cachedCsrfToken == null) {
            cachedCsrfToken = securePrefs.getString("csrf_token", null)
        }
        return cachedCsrfToken
    }

    suspend fun getAntiforgeryCookieSuspend(): String? {
        if (cachedAntiforgeryCookie == null) {
            cachedAntiforgeryCookie = securePrefs.getString("antiforgery_cookie", null)
        }
        return cachedAntiforgeryCookie
    }

    // Backward compatibility method
    fun getToken(): String? {
        return cachedApplicationToken
    }
    
    suspend fun saveToken(token: String) {
        saveApplicationToken(token)
    }
    
    suspend fun clearTokens() {
        securePrefs.edit().remove("application_token").remove("authentication_token").remove("csrf_token").remove("antiforgery_cookie").apply()
        cachedApplicationToken = null
        cachedAuthenticationToken = null
        cachedCsrfToken = null
        cachedAntiforgeryCookie = null
        android.util.Log.d("AuthDataStore", "Cleared all tokens and cookies")
    }

    suspend fun setCurrentUser(user: User) {
        // Log original businessId
        android.util.Log.d("AuthDataStore", "[setCurrentUser] User recibido: id=${user.id}, businessId=${user.businessId}")
        val finalBusinessId = user.businessId
        android.util.Log.d("AuthDataStore", "[setCurrentUser] BusinessId final a guardar: $finalBusinessId")
        
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_KEY] = gson.toJson(user.copy(businessId = finalBusinessId))
            preferences[PreferencesKeys.USER_ID] = user.id
            
            // Store tokens securely - keeping for backward compatibility
            preferences[PreferencesKeys.APPLICATION_TOKEN] = user.token
            preferences[PreferencesKeys.AUTHENTICATION_TOKEN] = user.refreshToken
            
            // Clear CSRF token and cookie on new login
            preferences.remove(PreferencesKeys.CSRF_TOKEN)
            preferences.remove(PreferencesKeys.ANTIFORGERY_COOKIE)
            
            // Store other user properties
            preferences[PreferencesKeys.EMAIL] = user.email
            preferences[PreferencesKeys.USERNAME] = user.username
            preferences[PreferencesKeys.FIRST_NAME] = user.firstName
            preferences[PreferencesKeys.LAST_NAME] = user.lastName
            preferences[PreferencesKeys.PHOTO_URL] = user.photoUrl ?: ""
            preferences[PreferencesKeys.ROLE] = user.role.name
            preferences[PreferencesKeys.PASSWORD] = user.password
            preferences[PreferencesKeys.IS_ONLINE] = true
            finalBusinessId?.let { preferences[PreferencesKeys.BUSINESS_ID] = it }
            user.siteId?.let { preferences[PreferencesKeys.SITE_ID] = it }
            user.systemOwnerId?.let { preferences[PreferencesKeys.SYSTEM_OWNER_ID] = it }
            val now = System.currentTimeMillis()
            preferences[PreferencesKeys.LAST_ACTIVE] = now.toString()
            lastActiveTime = now
        }
        
        // CRITICAL FIX: Store tokens in EncryptedSharedPreferences to match retrieval logic
        securePrefs.edit().apply {
            putString("application_token", user.token)
            putString("authentication_token", user.refreshToken)
            apply()
        }
        android.util.Log.d("AuthDataStore", "Tokens stored in secure preferences: ${user.token.take(10)}...")
        
        // Update cached tokens
        cachedApplicationToken = user.token
        cachedAuthenticationToken = user.refreshToken
        cachedCsrfToken = null
        cachedAntiforgeryCookie = null
        
        android.util.Log.d("AuthDataStore", "User data stored successfully")
    }

    suspend fun getCurrentUser(): User? {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.USER_KEY]?.let { userJson ->
            gson.fromJson(userJson, User::class.java)
        }
    }

    suspend fun clearAuth() {
        android.util.Log.d("AuthDataStore", "Clearing all auth data")
        updatePresence(false)
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        
        // CRITICAL FIX: Also clear secure preferences to match storage logic
        securePrefs.edit().apply {
            remove("application_token")
            remove("authentication_token")
            remove("csrf_token")
            remove("antiforgery_cookie")
            apply()
        }
        android.util.Log.d("AuthDataStore", "Cleared tokens from secure preferences")
        
        cachedApplicationToken = null
        cachedAuthenticationToken = null
        cachedCsrfToken = null
        cachedAntiforgeryCookie = null
        lastActiveTime = 0
    }

    suspend fun updatePresence(isOnline: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONLINE] = isOnline
            if (isOnline) {
                val now = System.currentTimeMillis()
                preferences[PreferencesKeys.LAST_ACTIVE] = now.toString()
                lastActiveTime = now
            }
        }
    }

    /**
     * Returns the expiration date of the current JWT token, or null if not available.
     */
    fun getTokenExpirationDate(): Date? {
        val token = getApplicationToken()
        Log.d("AuthDataStore", "[LOCK] getTokenExpirationDate() - token: ${token?.take(30)}...")
        if (token == null) {
            Log.w("AuthDataStore", "[LOCK] No token available for expiration check")
            return null
        }
        val parts = token.split(".")
        if (parts.size < 2) {
            Log.w("AuthDataStore", "[LOCK] Token format invalid for expiration check")
            return null
        }
        return try {
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP))
            val json = org.json.JSONObject(payload)
            val exp = json.optLong("exp", -1)
            Log.d("AuthDataStore", "[LOCK] JWT payload: $payload, exp: $exp")
            if (exp > 0) Date(exp * 1000) else null
        } catch (e: Exception) {
            Log.e("AuthDataStore", "[LOCK] Error parsing token expiration: ${e.message}", e)
            null
        }
    }

    /**
     * Logs the expiration date of the current JWT token for debugging.
     */
    fun logTokenExpirationDate() {
        val expiration = getTokenExpirationDate()
        android.util.Log.d("AuthDataStore", "JWT token expires at: $expiration")
    }
    
    // Business context methods
    suspend fun saveCurrentBusinessId(businessId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUSINESS_ID] = businessId
        }
        android.util.Log.d("AuthDataStore", "Saved business context: $businessId")
    }
    
    suspend fun getCurrentBusinessId(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.BUSINESS_ID]
    }
    
    suspend fun clearBusinessContext() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.BUSINESS_ID)
            preferences.remove(PreferencesKeys.SITE_ID)
        }
        android.util.Log.d("AuthDataStore", "Cleared business context")
    }

    /**
     * Save last activity timestamp for session validation
     */
    suspend fun updateLastActivity() {
        val now = System.currentTimeMillis()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_ACTIVE] = now.toString()
        }
        lastActiveTime = now
        Log.d("AuthDataStore", "Updated last activity: $now")
    }
    
    /**
     * Get last activity timestamp
     */
    suspend fun getLastActivity(): Long {
        if (lastActiveTime > 0) return lastActiveTime
        
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.LAST_ACTIVE]?.toLongOrNull() ?: 0L
    }
    
    /**
     * Check if session is still valid based on last activity
     */
    suspend fun isSessionValid(maxInactivityMs: Long = 24 * 60 * 60 * 1000L): Boolean { // 24 hours default
        val lastActivity = getLastActivity()
        if (lastActivity == 0L) return false
        
        val timeSinceLastActivity = System.currentTimeMillis() - lastActivity
        val isValid = timeSinceLastActivity <= maxInactivityMs
        
        Log.d("AuthDataStore", "Session validity check: last activity ${timeSinceLastActivity / 1000}s ago, valid: $isValid")
        return isValid
    }
    
    /**
     * Enhanced token validation with expiration check
     */
    fun isTokenValid(): Boolean {
        val token = getApplicationToken()
        Log.d("AuthDataStore", "[LOCK] isTokenValid() - token: ${token?.take(30)}...")
        if (token.isNullOrBlank()) return false
        val expiration = getTokenExpirationDate()
        Log.d("AuthDataStore", "[LOCK] isTokenValid() - expiration: $expiration")
        if (expiration == null) {
            Log.w("AuthDataStore", "[LOCK] Cannot determine token expiration")
            return true // Assume valid if we can't parse expiration
        }
        val timeUntilExpiration = expiration.time - System.currentTimeMillis()
        val isValid = timeUntilExpiration > 60_000L // At least 1 minute remaining
        Log.d("AuthDataStore", "[LOCK] isTokenValid() - timeUntilExpiration: $timeUntilExpiration, isValid: $isValid")
        return isValid
    }
} 