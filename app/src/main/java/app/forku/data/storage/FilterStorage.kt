package app.forku.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.filterDataStore: DataStore<Preferences> by preferencesDataStore(name = "admin_filter_prefs")

@Singleton
class FilterStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val businessIdKey = stringPreferencesKey("admin_filter_business_id")
    private val siteIdKey = stringPreferencesKey("admin_filter_site_id")
    private val allSitesKey = booleanPreferencesKey("admin_filter_all_sites")

    suspend fun saveBusinessId(businessId: String?) {
        android.util.Log.d("FilterStorage", "[FLOW] saveBusinessId() called with value: $businessId")
        context.filterDataStore.edit { prefs ->
            if (businessId != null) {
                prefs[businessIdKey] = businessId
            } else {
                prefs.remove(businessIdKey)
            }
        }
    }
    fun getBusinessId(): Flow<String?> =
        context.filterDataStore.data.map { 
            val value = it[businessIdKey]
            android.util.Log.d("FilterStorage", "[FLOW] getBusinessId() -> $value")
            value
        }

    suspend fun saveSiteId(siteId: String?) {
        android.util.Log.d("FilterStorage", "[FLOW] saveSiteId() called with value: $siteId")
        context.filterDataStore.edit { prefs ->
            if (siteId != null) {
                prefs[siteIdKey] = siteId
            } else {
                prefs.remove(siteIdKey)
            }
        }
    }
    fun getSiteId(): Flow<String?> =
        context.filterDataStore.data.map { 
            val value = it[siteIdKey]
            android.util.Log.d("FilterStorage", "[FLOW] getSiteId() -> $value")
            value
        }

    suspend fun saveAllSitesSelected(allSites: Boolean) {
        android.util.Log.d("FilterStorage", "[FLOW] saveAllSitesSelected() called with value: $allSites")
        context.filterDataStore.edit { prefs ->
            prefs[allSitesKey] = allSites
        }
    }
    fun getAllSitesSelected(): Flow<Boolean> =
        context.filterDataStore.data.map { 
            val value = it[allSitesKey] ?: false
            android.util.Log.d("FilterStorage", "[FLOW] getAllSitesSelected() -> $value")
            value
        }

    suspend fun clearFilters() {
        context.filterDataStore.edit { prefs ->
            prefs.remove(businessIdKey)
            prefs.remove(siteIdKey)
            prefs.remove(allSitesKey)
        }
    }
} 