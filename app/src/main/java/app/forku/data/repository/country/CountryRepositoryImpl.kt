package app.forku.data.repository.country

import android.util.Log
import app.forku.data.api.CountryApi
import app.forku.data.api.dto.country.toDto
import app.forku.domain.model.country.Country
import app.forku.domain.repository.country.CountryRepository
import app.forku.data.datastore.AuthDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryRepositoryImpl @Inject constructor(
    private val api: CountryApi,
    private val authDataStore: AuthDataStore
) : CountryRepository {

    override suspend fun getAllCountries(): List<Country> {
        return try {
            Log.d("CountryRepository", "Fetching all countries")
            val response = api.getAllCountries()
            if (!response.isSuccessful) {
                Log.e("CountryRepository", "Error fetching countries: ${response.code()}")
                emptyList()
            } else {
                response.body()?.map { it.toDomain() } ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("CountryRepository", "Error fetching countries", e)
            emptyList()
        }
    }

    override suspend fun getCountryById(id: String): Country? {
        return try {
            val response = api.getCountryById(id)
            if (!response.isSuccessful) {
                Log.e("CountryRepository", "Error getting country by id: ${response.code()}")
                null
            } else {
                response.body()?.toDomain()
            }
        } catch (e: Exception) {
            Log.e("CountryRepository", "Error getting country by id", e)
            null
        }
    }

    override suspend fun createCountry(country: Country): Country {
        val token = authDataStore.getApplicationToken()
        if (token.isNullOrBlank()) {
            throw Exception("Authentication token is missing. Please log in.")
        }
        val response = api.createCountry(country.toDto().copy(
            isNew = true,
            isDirty = true
        ))
        if (!response.isSuccessful) {
            throw Exception("Failed to create country: \\${response.code()}")
        }
        return response.body()?.toDomain() ?: throw Exception("Empty response body")
    }

    override suspend fun updateCountry(country: Country): Country {
        val token = authDataStore.getApplicationToken()
        if (token.isNullOrBlank()) {
            throw Exception("Authentication token is missing. Please log in.")
        }
        val response = api.updateCountry(country.toDto().copy(
            isDirty = true
        ))
        if (!response.isSuccessful) {
            throw Exception("Failed to update country: \\${response.code()}")
        }
        return response.body()?.toDomain() ?: throw Exception("Empty response body")
    }

    override suspend fun deleteCountry(id: String) {
        val response = api.deleteCountry(id)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete country: ${response.code()}")
        }
    }

    override suspend fun getActiveCountries(): List<Country> {
        return getAllCountries().filter { it.isActive }
    }
} 