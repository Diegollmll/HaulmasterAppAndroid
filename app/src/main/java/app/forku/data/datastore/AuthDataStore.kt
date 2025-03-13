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
    }

    @Volatile
    private var cachedToken: String? = null

    fun getToken(): String? {
        android.util.Log.d("AuthDataStore", "Getting cached token: $cachedToken")
        return cachedToken
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
            Setting current user:
            - ID: ${user.id}
            - Name: ${user.fullName}
            - Token: ${user.token.take(10)}...
            - Role: ${user.role}
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
        }
        cachedToken = user.token
        android.util.Log.d("AuthDataStore", "User data stored successfully")
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val preferences = context.dataStore.data.first()
            
            // Log all stored preferences for debugging
            android.util.Log.d("AuthDataStore", """
                Stored preferences:
                - USER_ID: ${preferences[PreferencesKeys.USER_ID]}
                - TOKEN: ${preferences[PreferencesKeys.TOKEN]?.take(10)}...
                - TOKEN_KEY: ${preferences[PreferencesKeys.TOKEN_KEY]?.take(10)}...
                - EMAIL: ${preferences[PreferencesKeys.EMAIL]}
                - USERNAME: ${preferences[PreferencesKeys.USERNAME]}
                - FIRST_NAME: ${preferences[PreferencesKeys.FIRST_NAME]}
                - LAST_NAME: ${preferences[PreferencesKeys.LAST_NAME]}
                - ROLE: ${preferences[PreferencesKeys.ROLE]}
            """.trimIndent())
            
            val userId = preferences[PreferencesKeys.USER_ID]
            android.util.Log.d("AuthDataStore", "Getting current user - Found ID: $userId")
            
            if (userId == null) {
                android.util.Log.e("AuthDataStore", "No user ID found in preferences")
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
                certifications = emptyList()
            ).also {
                android.util.Log.d("AuthDataStore", """
                    User retrieved successfully:
                    - ID: ${it.id}
                    - Name: ${it.fullName}
                    - Token: ${it.token.take(10)}...
                    - Role: ${it.role}
                """.trimIndent())
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthDataStore", "Error getting current user", e)
            null
        }
    }

    suspend fun clearAuth() {
        android.util.Log.d("AuthDataStore", "Clearing all auth data")
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        cachedToken = null
    }

    suspend fun initializeToken() {
        cachedToken = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.TOKEN_KEY]
        }.first()
        android.util.Log.d("AuthDataStore", "Initialized token: ${cachedToken?.take(10)}...")
    }
} 