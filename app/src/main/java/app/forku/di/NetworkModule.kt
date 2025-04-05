package app.forku.di

import app.forku.core.Constants

import app.forku.data.api.UserApi
import app.forku.data.api.VehicleApi
import app.forku.data.api.CertificationApi
import app.forku.data.api.FeedbackApi
import app.forku.data.api.WeatherApi
import app.forku.data.api.interceptor.AuthInterceptor
import app.forku.data.api.interceptor.RetryInterceptor
import app.forku.domain.repository.weather.WeatherRepository
import app.forku.data.repository.weather.WeatherRepositoryImpl
import app.forku.data.api.VehicleSessionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import java.util.concurrent.TimeUnit
import android.content.Context
import app.forku.core.network.NetworkConnectivityManager
import javax.inject.Named
import app.forku.data.api.IncidentApi
import app.forku.data.api.ChecklistApi
import app.forku.data.api.SessionApi
import app.forku.data.api.NotificationApi
import app.forku.data.remote.api.BusinessApi


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideVehicleApi(retrofit: Retrofit): VehicleApi = retrofit.create(VehicleApi::class.java)

    @Provides
    @Singleton
    fun provideIncidentApi(retrofit: Retrofit): IncidentApi = retrofit.create(IncidentApi::class.java)

    @Provides
    @Singleton
    fun provideChecklistApi(retrofit: Retrofit): ChecklistApi = retrofit.create(ChecklistApi::class.java)

    @Provides
    @Singleton
    fun provideSessionApi(retrofit: Retrofit): SessionApi = retrofit.create(SessionApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideCertificationApi(retrofit: Retrofit): CertificationApi = retrofit.create(CertificationApi::class.java)

    @Provides
    @Singleton
    fun provideFeedbackApi(retrofit: Retrofit): FeedbackApi = retrofit.create(FeedbackApi::class.java)

    @Provides
    @Singleton
    fun provideWeatherApi(retrofit: Retrofit): WeatherApi = retrofit.create(WeatherApi::class.java)

    @Provides
    @Singleton
    fun provideVehicleSessionApi(retrofit: Retrofit): VehicleSessionApi = 
        retrofit.create(VehicleSessionApi::class.java)

    @Provides
    @Singleton
    fun provideBusinessApi(retrofit: Retrofit): BusinessApi {
        return retrofit.create(BusinessApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        @Named("baseUrl") baseUrl: String
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @Named("apiKey") apiKey: String
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-API-KEY", apiKey)
                .build()
            chain.proceed(request)
        }
        .build()

    @Provides
    @Named("baseUrl")
    fun provideBaseUrl(): String = Constants.BASE_URL

    @Provides
    @Named("apiKey")
    fun provideApiKey(): String = ""

    @Provides
    @Singleton
    fun provideWeatherRepository(
        weatherApi: WeatherApi
    ): WeatherRepository {
        return WeatherRepositoryImpl(weatherApi)
    }

    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(
        @ApplicationContext context: Context
    ): NetworkConnectivityManager {
        return NetworkConnectivityManager(context)
    }
}