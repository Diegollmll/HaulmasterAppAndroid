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
import app.forku.data.service.GOServicesManager
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
import app.forku.data.api.BusinessApi
import javax.inject.Named
import javax.inject.Provider
import app.forku.data.api.IncidentApi
import app.forku.data.api.ChecklistApi
import app.forku.data.api.SessionApi
import app.forku.data.api.NotificationApi

import app.forku.data.api.CountryApi
import app.forku.data.api.CountryStateApi

import app.forku.data.api.VehicleTypeApi
import app.forku.data.api.EnergySourceApi
import app.forku.data.api.SiteApi
import app.forku.data.api.VehicleCategoryApi
import app.forku.data.api.VehicleComponentApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.api.GOFileUploaderApi
import app.forku.data.api.GOGroupApi
import app.forku.data.api.GOGroupRoleApi
import app.forku.data.api.GOUserRoleApi
import app.forku.data.api.UserBusinessApi
import app.forku.data.api.BusinessConfigurationApi

//import app.forku.data.api.CicoHistoryApi


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
    fun provideVehicleCategoryApi(retrofit: Retrofit): VehicleCategoryApi =
        retrofit.create(VehicleCategoryApi::class.java)

    @Provides
    @Singleton
    fun provideVehicleTypeApi(retrofit: Retrofit): VehicleTypeApi = 
        retrofit.create(VehicleTypeApi::class.java)

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
    fun provideBusinessConfigurationApi(retrofit: Retrofit): BusinessConfigurationApi =
        retrofit.create(BusinessConfigurationApi::class.java)

    @Provides
    @Singleton
    fun provideUserBusinessApi(retrofit: Retrofit): UserBusinessApi {
        return retrofit.create(UserBusinessApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCountryApi(retrofit: Retrofit): CountryApi =
        retrofit.create(CountryApi::class.java)

    @Provides
    @Singleton
    fun provideStateApi(retrofit: Retrofit): CountryStateApi =
        retrofit.create(CountryStateApi::class.java)

    @Provides
    @Singleton
    fun provideEnergySourceApi(retrofit: Retrofit): EnergySourceApi =
        retrofit.create(EnergySourceApi::class.java)

    @Provides
    @Singleton
    fun provideSiteApi(retrofit: Retrofit): SiteApi {
        return retrofit.create(SiteApi::class.java)
    }

//    @Provides
//    @Singleton
//    fun provideCicoHistoryApi(retrofit: Retrofit): CicoHistoryApi =
//        retrofit.create(CicoHistoryApi::class.java)

    @Provides
    @Singleton
    fun provideVehicleComponentApi(retrofit: Retrofit): VehicleComponentApi {
        return retrofit.create(VehicleComponentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGOFileUploaderApi(retrofit: Retrofit): GOFileUploaderApi {
        return retrofit.create(GOFileUploaderApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGOGroupApi(retrofit: Retrofit): GOGroupApi {
        return retrofit.create(GOGroupApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGOGroupRoleApi(retrofit: Retrofit): GOGroupRoleApi {
        return retrofit.create(GOGroupRoleApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGOUserRoleApi(retrofit: Retrofit): GOUserRoleApi {
        return retrofit.create(GOUserRoleApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        authDataStore: AuthDataStore,
        goServicesManagerProvider: Provider<GOServicesManager>
    ): AuthInterceptor {
        return AuthInterceptor(authDataStore, goServicesManagerProvider)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        retryInterceptor: RetryInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(retryInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

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