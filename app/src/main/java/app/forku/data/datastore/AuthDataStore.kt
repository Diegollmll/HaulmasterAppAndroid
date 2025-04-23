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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

@Singleton
class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val USER_KEY = stringPreferencesKey("user")
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val USER_ID = stringPreferencesKey("userId")
        val TOKEN = stringPreferencesKey("token")
        val REFRESH_TOKEN = stringPreferencesKey("refreshToken")
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

    private var cachedToken: String? = null
    private var lastActiveTime: Long = 0
    private val gson = Gson()

    val token: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TOKEN_KEY]
        }

    suspend fun initializeToken() {
        android.util.Log.d("AuthDataStore", "Initializing token...")
        cachedToken = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.TOKEN_KEY]
        }.first()
        android.util.Log.d("AuthDataStore", "Token initialized: ${cachedToken?.take(10)}...")
    }

    fun getToken(): String? {
        android.util.Log.d("AuthDataStore", "Getting cached token: $cachedToken")
        return cachedToken
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOKEN_KEY] = token
        }
        cachedToken = token
        android.util.Log.d("AuthDataStore", "Saved token: ${token.take(10)}...")
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.TOKEN_KEY)
        }
        cachedToken = null
        android.util.Log.d("AuthDataStore", "Cleared token")
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
            preferences[PreferencesKeys.TOKEN] = user.token
            preferences[PreferencesKeys.REFRESH_TOKEN] = user.refreshToken
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
        cachedToken = user.token
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
        cachedToken = null
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
} 