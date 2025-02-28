package app.forku.data.repository.weather

import app.forku.data.api.WeatherApi
import app.forku.domain.model.weather.Weather
import app.forku.domain.repository.weather.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi
) : WeatherRepository {
    override suspend fun getWeatherByCoordinates(
        latitude: Double, 
        longitude: Double
    ): Result<Weather> = try {
        val response = weatherApi.getWeather("$latitude,$longitude")
        if (response.isSuccessful) {
            val weather = response.body()?.let { dto ->
                Weather(
                    description = dto.current.condition.text,
                    temperature = dto.current.tempC,
                    humidity = dto.current.humidity,
                    windSpeed = dto.current.windKph
                )
            } ?: throw Exception("Empty response")
            Result.success(weather)
        } else {
            Result.failure(Exception("Failed to fetch weather"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
} 