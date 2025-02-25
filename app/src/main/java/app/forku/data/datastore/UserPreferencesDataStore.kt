package app.forku.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.forku.data.api.dto.user.UserDto
import app.forku.data.mapper.toDomain
import app.forku.domain.model.user.User
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val userKey = stringPreferencesKey("user")

    suspend fun saveUser(userDto: UserDto) {
        context.dataStore.edit { preferences ->
            preferences[userKey] = gson.toJson(userDto)
        }
    }

    fun getUser(): Flow<User?> {
        return context.dataStore.data.map { preferences ->
            preferences[userKey]?.let { userJson ->
                gson.fromJson(userJson, UserDto::class.java).toDomain()
            }
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(userKey)
        }
    }
} 