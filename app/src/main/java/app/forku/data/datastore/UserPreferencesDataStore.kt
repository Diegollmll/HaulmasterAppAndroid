package app.forku.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
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
    
    // Admin filter preferences keys
    private val adminFilterBusinessIdKey = stringPreferencesKey("admin_filter_business_id")
    private val adminFilterSiteIdKey = stringPreferencesKey("admin_filter_site_id")
    private val adminFilterAllSitesKey = booleanPreferencesKey("admin_filter_all_sites")

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
    
    // Admin Filter Methods
    suspend fun setAdminFilterBusinessId(businessId: String?) {
        context.dataStore.edit { preferences ->
            if (businessId != null) {
                preferences[adminFilterBusinessIdKey] = businessId
            } else {
                preferences.remove(adminFilterBusinessIdKey)
            }
        }
    }
    
    fun getAdminFilterBusinessId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[adminFilterBusinessIdKey]
        }
    }
    
    suspend fun setAdminFilterSiteId(siteId: String?) {
        context.dataStore.edit { preferences ->
            if (siteId != null) {
                preferences[adminFilterSiteIdKey] = siteId
            } else {
                preferences.remove(adminFilterSiteIdKey)
            }
        }
    }
    
    fun getAdminFilterSiteId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[adminFilterSiteIdKey]
        }
    }
    
    suspend fun setAdminFilterAllSites(isAllSites: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[adminFilterAllSitesKey] = isAllSites
        }
    }
    
    fun getAdminFilterAllSites(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[adminFilterAllSitesKey] ?: false
        }
    }
    
    suspend fun clearAdminFilters() {
        context.dataStore.edit { preferences ->
            preferences.remove(adminFilterBusinessIdKey)
            preferences.remove(adminFilterSiteIdKey)
            preferences.remove(adminFilterAllSitesKey)
        }
    }
} 