package app.forku.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GOServicesPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val CSRF_TOKEN = stringPreferencesKey("go_services_csrf_token")
    }

    val csrfToken: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[CSRF_TOKEN]
        }

    suspend fun setCsrfToken(token: String) {
        dataStore.edit { preferences ->
            preferences[CSRF_TOKEN] = token
        }
    }

    suspend fun clearCsrfToken() {
        dataStore.edit { preferences ->
            preferences.remove(CSRF_TOKEN)
        }
    }
} 