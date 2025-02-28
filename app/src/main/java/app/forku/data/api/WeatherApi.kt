package app.forku.data.api

import app.forku.BuildConfig
import app.forku.data.api.dto.weather.WeatherResponseDto


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("current.json")
    suspend fun getWeather(
        @Query("q") query: String,
        @Query("key") apiKey: String = BuildConfig.WEATHER_API_KEY,
    ): Response<WeatherResponseDto>
} 