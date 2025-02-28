package app.forku.domain.repository.weather

import app.forku.domain.model.weather.Weather

interface WeatherRepository {
    suspend fun getWeatherByCoordinates(latitude: Double, longitude: Double): Result<Weather>
} 