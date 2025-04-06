package app.forku.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

@Singleton
class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    private object PreferencesKeys {
        val USER_ID = stringPreferencesKey("user_id")
        val TOKEN = stringPreferencesKey("token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val EMAIL = stringPreferencesKey("email")
        val USERNAME = stringPreferencesKey("username")
        val FIRST_NAME = stringPreferencesKey("first_name")
        val LAST_NAME = stringPreferencesKey("last_name")
        val PHOTO_URL = stringPreferencesKey("photo_url")
        val ROLE = stringPreferencesKey("role")
        val USER_KEY = stringPreferencesKey("user")
        val TOKEN_KEY = stringPreferencesKey("token")
        val PASSWORD = stringPreferencesKey("password")
        val LAST_ACTIVE = stringPreferencesKey("last_active")
        val IS_ONLINE = booleanPreferencesKey("is_online")
        val BUSINESS_ID = stringPreferencesKey("business_id")
        val SITE_ID = stringPreferencesKey("site_id")
        val SYSTEM_OWNER_ID = stringPreferencesKey("system_owner_id")
    }

    @Volatile
    private var cachedToken: String? = null
    private var lastActiveTime: Long = 0

    fun getToken(): String? {
        android.util.Log.d("AuthDataStore", "Getting cached token: $cachedToken")
        return cachedToken
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

    suspend fun setToken(token: String?) {
        android.util.Log.d("AuthDataStore", "Setting token: ${token?.take(10)}...")
        cachedToken = token
        context.dataStore.edit { preferences ->
            if (token != null) {
                preferences[PreferencesKeys.TOKEN_KEY] = token
            } else {
                preferences.remove(PreferencesKeys.TOKEN_KEY)
            }
        }
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
            preferences[PreferencesKeys.USER_ID] = user.id
            preferences[PreferencesKeys.TOKEN] = user.token
            preferences[PreferencesKeys.REFRESH_TOKEN] = user.refreshToken
            preferences[PreferencesKeys.EMAIL] = user.email
            preferences[PreferencesKeys.USERNAME] = user.username
            preferences[PreferencesKeys.FIRST_NAME] = user.firstName
            preferences[PreferencesKeys.LAST_NAME] = user.lastName
            preferences[PreferencesKeys.PHOTO_URL] = user.photoUrl ?: ""
            preferences[PreferencesKeys.ROLE] = user.role.name
            preferences[PreferencesKeys.TOKEN_KEY] = user.token
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
        return try {
            val preferences = context.dataStore.data.first()
            
            android.util.Log.d("AuthDataStore", """
                Current preferences:
                - USER_ID: ${preferences[PreferencesKeys.USER_ID]}
                - EMAIL: ${preferences[PreferencesKeys.EMAIL]}
                - USERNAME: ${preferences[PreferencesKeys.USERNAME]}
                - FIRST_NAME: ${preferences[PreferencesKeys.FIRST_NAME]}
                - LAST_NAME: ${preferences[PreferencesKeys.LAST_NAME]}
                - ROLE: ${preferences[PreferencesKeys.ROLE]}
                - IS_ONLINE: ${preferences[PreferencesKeys.IS_ONLINE]}
                - LAST_ACTIVE: ${preferences[PreferencesKeys.LAST_ACTIVE]}
                - BUSINESS_ID: ${preferences[PreferencesKeys.BUSINESS_ID]}
                - SITE_ID: ${preferences[PreferencesKeys.SITE_ID]}
                - SYSTEM_OWNER_ID: ${preferences[PreferencesKeys.SYSTEM_OWNER_ID]}
            """.trimIndent())
            
            val userId = preferences[PreferencesKeys.USER_ID] ?: run {
                android.util.Log.e("AuthDataStore", "No user ID found")
                return null
            }
            val token = preferences[PreferencesKeys.TOKEN] ?: run {
                android.util.Log.e("AuthDataStore", "No token found for user $userId")
                return null
            }
            val refreshToken = preferences[PreferencesKeys.REFRESH_TOKEN] ?: run {
                android.util.Log.e("AuthDataStore", "No refresh token found for user $userId")
                return null
            }
            val email = preferences[PreferencesKeys.EMAIL] ?: run {
                android.util.Log.e("AuthDataStore", "No email found for user $userId")
                return null
            }
            val username = preferences[PreferencesKeys.USERNAME] ?: run {
                android.util.Log.e("AuthDataStore", "No username found for user $userId")
                return null
            }
            val firstName = preferences[PreferencesKeys.FIRST_NAME] ?: run {
                android.util.Log.e("AuthDataStore", "No first name found for user $userId")
                return null
            }
            val lastName = preferences[PreferencesKeys.LAST_NAME] ?: run {
                android.util.Log.e("AuthDataStore", "No last name found for user $userId")
                return null
            }
            val photoUrl = preferences[PreferencesKeys.PHOTO_URL]
            val role = preferences[PreferencesKeys.ROLE]?.let { UserRole.fromString(it) } ?: run {
                android.util.Log.e("AuthDataStore", "No role found for user $userId")
                return null
            }
            val password = preferences[PreferencesKeys.PASSWORD] ?: ""
            val businessId = preferences[PreferencesKeys.BUSINESS_ID]
            val siteId = preferences[PreferencesKeys.SITE_ID]
            val systemOwnerId = preferences[PreferencesKeys.SYSTEM_OWNER_ID]

            val isOnline = preferences[PreferencesKeys.IS_ONLINE] ?: false
            val lastActive = preferences[PreferencesKeys.LAST_ACTIVE]?.toLongOrNull() ?: 0L

            User(
                id = userId,
                token = token,
                refreshToken = refreshToken,
                email = email,
                username = username,
                firstName = "$firstName",
                lastName = "$lastName",
                photoUrl = photoUrl?.takeIf { it.isNotEmpty() },
                role = role,
                certifications = emptyList(),
                password = password,
                isActive = isOnline,
                lastLogin = lastActive.toString(),
                businessId = businessId,
                siteId = siteId,
                systemOwnerId = systemOwnerId
            ).also {
                android.util.Log.d("AuthDataStore", """
                    User retrieved successfully:
                    - ID: ${it.id}
                    - Name: ${it.fullName}
                    - Token: ${it.token.take(10)}...
                    - Role: ${it.role}
                    - Business ID: ${it.businessId}
                    - Site ID: ${it.siteId}
                    - System Owner ID: ${it.systemOwnerId}
                    - Online: $isOnline
                    - Last Active: ${java.time.Instant.ofEpochMilli(lastActive)}
                """.trimIndent())
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthDataStore", "Error getting current user", e)
            null
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

    suspend fun initializeToken() {
        cachedToken = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.TOKEN_KEY]
        }.first()
        android.util.Log.d("AuthDataStore", "Initialized token: ${cachedToken?.take(10)}...")
    }
} 