package app.forku.domain.repository.country

import app.forku.domain.model.country.CountryState

interface StateRepository {
    suspend fun getAllStates(): List<CountryState>
    suspend fun getStatesByCountry(countryId: String): List<CountryState>
    suspend fun getStateById(id: String): CountryState
    suspend fun createState(countryState: CountryState)
    suspend fun updateState(countryState: CountryState)
    suspend fun deleteState(id: String)
} 