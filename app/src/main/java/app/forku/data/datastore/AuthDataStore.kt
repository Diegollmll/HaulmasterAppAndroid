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
        val NAME = stringPreferencesKey("name")
        val PHOTO_URL = stringPreferencesKey("photo_url")
        val ROLE = stringPreferencesKey("role")
        val PERMISSIONS = stringSetPreferencesKey("permissions")
        val USER_KEY = stringPreferencesKey("user")
        val TOKEN_KEY = stringPreferencesKey("token")
    }

    @Volatile
    private var cachedToken: String? = null

    fun getToken(): String? = cachedToken

    suspend fun setToken(token: String?) {
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
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = user.id
            preferences[PreferencesKeys.TOKEN] = user.token
            preferences[PreferencesKeys.REFRESH_TOKEN] = user.refreshToken
            preferences[PreferencesKeys.EMAIL] = user.email
            preferences[PreferencesKeys.USERNAME] = user.username
            preferences[PreferencesKeys.NAME] = user.name
            preferences[PreferencesKeys.PHOTO_URL] = user.photoUrl ?: ""
            preferences[PreferencesKeys.ROLE] = user.role.name
            preferences[PreferencesKeys.PERMISSIONS] = user.permissions.toSet()
            preferences[PreferencesKeys.TOKEN_KEY] = user.token
        }
        cachedToken = user.token
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val preferences = context.dataStore.data.first()
            val userId = preferences[PreferencesKeys.USER_ID] ?: return null
            val token = preferences[PreferencesKeys.TOKEN] ?: return null
            val refreshToken = preferences[PreferencesKeys.REFRESH_TOKEN] ?: return null
            val email = preferences[PreferencesKeys.EMAIL] ?: return null
            val username = preferences[PreferencesKeys.USERNAME] ?: return null
            val name = preferences[PreferencesKeys.NAME] ?: return null
            val photoUrl = preferences[PreferencesKeys.PHOTO_URL]
            val role = preferences[PreferencesKeys.ROLE]?.let { UserRole.fromString(it) } ?: return null
            val permissions = preferences[PreferencesKeys.PERMISSIONS]?.toList() ?: emptyList()

            User(
                id = userId,
                token = token,
                refreshToken = refreshToken,
                email = email,
                username = username,
                name = name,
                photoUrl = photoUrl?.takeIf { it.isNotEmpty() },
                role = role,
                permissions = permissions,
                certifications = emptyList()
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun initializeToken() {
        cachedToken = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.TOKEN_KEY]
        }.first()
    }
} 