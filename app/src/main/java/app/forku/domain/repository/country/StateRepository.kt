package app.forku.domain.repository.country

import app.forku.domain.model.country.State

interface StateRepository {
    suspend fun getAllStates(): List<State>
    suspend fun getStatesByCountry(countryId: String): List<State>
    suspend fun getStateById(id: String): State
    suspend fun createState(state: State)
    suspend fun updateState(state: State)
    suspend fun deleteState(id: String)
} 