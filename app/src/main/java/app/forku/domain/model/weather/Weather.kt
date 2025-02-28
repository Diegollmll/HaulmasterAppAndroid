package app.forku.domain.model.weather

data class Weather(
    val description: String,
    val temperature: Double,
    val humidity: Long,
    val windSpeed: Double
) 