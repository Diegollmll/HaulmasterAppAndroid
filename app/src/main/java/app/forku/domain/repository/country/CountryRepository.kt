package app.forku.domain.repository.country

import app.forku.domain.model.country.Country

interface CountryRepository {
    suspend fun getAllCountries(): List<Country>
    suspend fun getCountryById(id: String): Country?
    suspend fun createCountry(country: Country): Country
    suspend fun updateCountry(country: Country): Country
    suspend fun deleteCountry(id: String)
    suspend fun getActiveCountries(): List<Country>
} 