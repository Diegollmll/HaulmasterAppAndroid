package app.forku.data.repository.country

import android.util.Log
import app.forku.data.api.CountryStateApi
import app.forku.data.api.dto.country.toDto
import app.forku.data.datastore.AuthDataStore

import app.forku.domain.model.country.CountryState
import app.forku.domain.repository.country.StateRepository
import javax.inject.Inject

class StateRepositoryImpl @Inject constructor(
    private val countryStateApi: CountryStateApi,
    private val authDataStore: AuthDataStore
) : StateRepository {

    override suspend fun getAllStates(): List<CountryState> {
        val response = countryStateApi.getAllStates()
        Log.d("StateRepository", "getAllStates response: ${response.code()}")
        return if (response.isSuccessful) {
            response.body()?.map { it.toDomain() } ?: emptyList()
        } else {
            Log.e("StateRepository", "Failed to get all states: ${response.code()}")
            emptyList()
        }
    }

    override suspend fun getStatesByCountry(countryId: String): List<CountryState> {
        // This endpoint is no longer available in the new API
        return getAllStates().filter { it.countryId == countryId }
    }

    override suspend fun getStateById(id: String): CountryState {
        val response = countryStateApi.getStateById(id)
        Log.d("StateRepository", "getStateById($id) response: ${response.code()}")
        if (!response.isSuccessful) {
            Log.e("StateRepository", "Failed to get state $id: ${response.code()}")
            throw Exception("Failed to get state: ${response.code()}")
        }
        return response.body()?.toDomain() ?: throw Exception("State not found")
    }

    override suspend fun createState(countryState: CountryState) {
        val token = authDataStore.getApplicationToken()
        if (token.isNullOrBlank()) {
            throw Exception("Authentication token is missing. Please log in.")
        }
        val response = countryStateApi.createState(countryState.toDto().copy(
            isNew = true,
            isDirty = true
        ))
        Log.d("StateRepository", "createState response: ${response.code()}")
        if (!response.isSuccessful) {
            Log.e("StateRepository", "Failed to create state: ${response.code()}")
            throw Exception("Failed to create state: ${response.code()}")
        }
    }

    override suspend fun updateState(countryState: CountryState) {
        val token = authDataStore.getApplicationToken()
        if (token.isNullOrBlank()) {
            throw Exception("Authentication token is missing. Please log in.")
        }
        val response = countryStateApi.updateState(countryState.toDto().copy(
            isDirty = true
        ))
        Log.d("StateRepository", "updateState(${countryState.id}) response: ${response.code()}")
        if (!response.isSuccessful) {
            Log.e("StateRepository", "Failed to update state ${countryState.id}: ${response.code()}")
            throw Exception("Failed to update state: ${response.code()}")
        }
    }

    override suspend fun deleteState(id: String) {
        val response = countryStateApi.deleteState(id)
        Log.d("StateRepository", "deleteState($id) response: ${response.code()}")
        if (!response.isSuccessful) {
            Log.e("StateRepository", "Failed to delete state $id: ${response.code()}")
            throw Exception("Failed to delete state: ${response.code()}")
        }
    }
} 