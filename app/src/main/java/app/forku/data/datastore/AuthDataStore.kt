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
        android.util.Log.d("AuthDataStore", "Application token initialized: "+(cachedApplicationToken?.take(10))+"...")
    }
    
    fun getApplicationToken(): String? {
        android.util.Log.d("AuthDataStore", "Getting cached application token: "+(cachedApplicationToken?.take(10)))
        return cachedApplicationToken ?: securePrefs.getString("application_token", null)
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
        return cachedAuthenticationToken ?: securePrefs.getString("authentication_token", null)
    }

    // CSRF Token methods
    suspend fun saveCsrfToken(token: String) {
        securePrefs.edit().putString("csrf_token", token).apply()
        cachedCsrfToken = token
        android.util.Log.d("AuthDataStore", "Saved CSRF token: "+token.take(10)+"...")
    }
    
    fun getCsrfToken(): String? = cachedCsrfToken ?: securePrefs.getString("csrf_token", null)

    // Antiforgery Cookie methods
    suspend fun saveAntiforgeryCookie(cookie: String) {
        securePrefs.edit().putString("antiforgery_cookie", cookie).apply()
        cachedAntiforgeryCookie = cookie
        android.util.Log.d("AuthDataStore", "Saved Antiforgery cookie: "+cookie.take(20)+"...")
    }
    
    fun getAntiforgeryCookie(): String? = cachedAntiforgeryCookie ?: securePrefs.getString("antiforgery_cookie", null)

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
        android.util.Log.d("AuthDataStore", """
            Storing user data:
            - ID: ${user.id}
            - Name: ${user.fullName}
            - Token: ${user.token.take(10)}...
            - Role: ${user.role}
            - Business ID: ${user.businessId}
            - Site ID: ${user.siteId}
            - System Owner ID: ${user.systemOwnerId}
        """.trimIndent())
        
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_KEY] = gson.toJson(user)
            preferences[PreferencesKeys.USER_ID] = user.id
            
            // Store tokens securely
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
            user.businessId?.let { preferences[PreferencesKeys.BUSINESS_ID] = it }
            user.siteId?.let { preferences[PreferencesKeys.SITE_ID] = it }
            user.systemOwnerId?.let { preferences[PreferencesKeys.SYSTEM_OWNER_ID] = it }
            val now = System.currentTimeMillis()
            preferences[PreferencesKeys.LAST_ACTIVE] = now.toString()
            lastActiveTime = now
        }
        
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
        val token = getApplicationToken() ?: return null
        val parts = token.split(".")
        if (parts.size < 2) return null
        return try {
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
            val json = JSONObject(payload)
            val exp = json.optLong("exp", -1)
            if (exp > 0) Date(exp * 1000) else null
        } catch (e: Exception) {
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
} 