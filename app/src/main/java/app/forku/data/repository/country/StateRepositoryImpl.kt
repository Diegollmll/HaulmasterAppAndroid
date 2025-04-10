package app.forku.data.repository.country

import android.util.Log
import app.forku.data.api.StateApi
import app.forku.data.api.dto.country.toDto
import app.forku.data.api.dto.country.toDomain
import app.forku.domain.model.country.State
import app.forku.domain.repository.country.StateRepository
import javax.inject.Inject

class StateRepositoryImpl @Inject constructor(
    private val stateApi: StateApi
) : StateRepository {

    override suspend fun getAllStates(): List<State> {
        val response = stateApi.getAllStates()
        Log.d("StateRepository", "getAllStates response: ${response.code()}")
        return if (response.isSuccessful) {
            response.body()?.map { it.toDomain() } ?: emptyList()
        } else {
            Log.e("StateRepository", "Failed to get all states: ${response.code()}")
            emptyList()
        }
    }

    override suspend fun getStatesByCountry(countryId: String): List<State> {
        val response = stateApi.getStatesByCountry(countryId)
        Log.d("StateRepository", "getStatesByCountry($countryId) response: ${response.code()}")
        return if (response.isSuccessful) {
            val states = response.body()?.map { it.toDomain() } ?: emptyList()
            Log.d("StateRepository", "Found ${states.size} states for country $countryId")
            states
        } else {
            Log.e("StateRepository", "Failed to get states for country $countryId: ${response.code()}")
            emptyList()
        }
    }

    override suspend fun getStateById(id: String): State {
        val response = stateApi.getStateById(id)
        Log.d("StateRepository", "getStateById($id) response: ${response.code()}")
        if (!response.isSuccessful) {
            Log.e("StateRepository", "Failed to get state $id: ${response.code()}")
            throw Exception("Failed to get state: ${response.code()}")
        }
        return response.body()?.toDomain() ?: throw Exception("State not found")
    }

    override suspend fun createState(state: State) {
        val response = stateApi.createState(state.toDto())
        Log.d("StateRepository", "createState response: ${response.code()}")
        if (!response.isSuccessful) {
            Log.e("StateRepository", "Failed to create state: ${response.code()}")
            throw Exception("Failed to create state: ${response.code()}")
        }
    }

    override suspend fun updateState(state: State) {
        val response = stateApi.updateState(state.id, state.toDto())
        Log.d("StateRepository", "updateState(${state.id}) response: ${response.code()}")
        if (!response.isSuccessful) {
            Log.e("StateRepository", "Failed to update state ${state.id}: ${response.code()}")
            throw Exception("Failed to update state: ${response.code()}")
        }
    }

    override suspend fun deleteState(id: String) {
        val response = stateApi.deleteState(id)
        Log.d("StateRepository", "deleteState($id) response: ${response.code()}")
        if (!response.isSuccessful) {
            Log.e("StateRepository", "Failed to delete state $id: ${response.code()}")
            throw Exception("Failed to delete state: ${response.code()}")
        }
    }
} 