package app.forku.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AuthDataStore @Inject constructor(
    private val context: Context
) {
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
        }
    }

    suspend fun getCurrentUser(): User? {
        return context.dataStore.data.map { preferences ->
            if (preferences[PreferencesKeys.USER_ID] == null) return@map null
            
            User(
                id = preferences[PreferencesKeys.USER_ID]!!,
                token = preferences[PreferencesKeys.TOKEN]!!,
                refreshToken = preferences[PreferencesKeys.REFRESH_TOKEN]!!,
                email = preferences[PreferencesKeys.EMAIL]!!,
                username = preferences[PreferencesKeys.USERNAME]!!,
                name = preferences[PreferencesKeys.NAME]!!,
                photoUrl = preferences[PreferencesKeys.PHOTO_URL]?.takeIf { it.isNotEmpty() },
                role = UserRole.valueOf(preferences[PreferencesKeys.ROLE]!!),
                permissions = preferences[PreferencesKeys.PERMISSIONS]?.toList() ?: emptyList(),
                certifications = emptyList()
            )
        }.first()
    }

    suspend fun clearAuth() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
} 