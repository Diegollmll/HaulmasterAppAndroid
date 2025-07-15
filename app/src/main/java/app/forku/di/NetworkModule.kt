package app.forku.di

import app.forku.core.Constants
import app.forku.core.auth.HeaderManager
import app.forku.data.api.*
import app.forku.data.api.interceptor.AuthInterceptor
import app.forku.data.api.interceptor.RetryInterceptor
import app.forku.domain.repository.weather.WeatherRepository
import app.forku.data.repository.weather.WeatherRepositoryImpl
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
import app.forku.core.auth.TokenErrorHandler
import app.forku.core.network.NetworkConnectivityManager
import javax.inject.Named
import app.forku.data.api.interceptor.FormUrlEncodedInterceptor
import app.forku.domain.repository.incident.IncidentMultimediaRepository
import app.forku.data.repository.incident.IncidentMultimediaRepositoryImpl
import app.forku.data.api.ChecklistItemAnswerMultimediaApi
import app.forku.data.repository.checklist.ChecklistItemAnswerMultimediaRepositoryImpl
import app.forku.domain.repository.checklist.ChecklistItemAnswerMultimediaRepository
import app.forku.core.business.BusinessContextManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    @Provides
    @Singleton
    @Named("baseClient")
    fun provideBaseOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        retryInterceptor: RetryInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(retryInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("baseRetrofit")
    fun provideBaseRetrofit(@Named("baseClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        headerManager: HeaderManager,
        tokenErrorHandler: TokenErrorHandler,
        sessionKeepAliveManager: app.forku.core.auth.SessionKeepAliveManager? = null
    ): AuthInterceptor {
        return AuthInterceptor(headerManager, tokenErrorHandler, sessionKeepAliveManager)
    }

    @Provides
    @Singleton
    @Named("authenticatedClient")
    fun provideAuthenticatedOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        retryInterceptor: RetryInterceptor,
        formUrlEncodedInterceptor: FormUrlEncodedInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(retryInterceptor)
            .addInterceptor(formUrlEncodedInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("authenticatedRetrofit")
    fun provideAuthenticatedRetrofit(@Named("authenticatedClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideUserApi(@Named("authenticatedRetrofit") retrofit: Retrofit): UserApi = 
        retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideVehicleApi(@Named("authenticatedRetrofit") retrofit: Retrofit): VehicleApi = 
        retrofit.create(VehicleApi::class.java)

    @Provides
    @Singleton
    fun provideVehicleCategoryApi(@Named("authenticatedRetrofit") retrofit: Retrofit): VehicleCategoryApi =
        retrofit.create(VehicleCategoryApi::class.java)

    @Provides
    @Singleton
    fun provideVehicleTypeApi(@Named("authenticatedRetrofit") retrofit: Retrofit): VehicleTypeApi = 
        retrofit.create(VehicleTypeApi::class.java)

    @Provides
    @Singleton
    fun provideIncidentApi(@Named("authenticatedRetrofit") retrofit: Retrofit): IncidentApi = 
        retrofit.create(IncidentApi::class.java)

    @Provides
    @Singleton
    fun provideChecklistApi(@Named("authenticatedRetrofit") retrofit: Retrofit): ChecklistApi = 
        retrofit.create(ChecklistApi::class.java)

    @Provides
    @Singleton
    fun provideSessionApi(@Named("authenticatedRetrofit") retrofit: Retrofit): SessionApi = 
        retrofit.create(SessionApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(@Named("authenticatedRetrofit") retrofit: Retrofit): NotificationApi = 
        retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideCertificationApi(@Named("authenticatedRetrofit") retrofit: Retrofit): CertificationApi = 
        retrofit.create(CertificationApi::class.java)

    @Provides
    @Singleton
    fun provideFeedbackApi(@Named("authenticatedRetrofit") retrofit: Retrofit): FeedbackApi = 
        retrofit.create(FeedbackApi::class.java)

    @Provides
    @Singleton
    @Named("weatherRetrofit")
    fun provideWeatherRetrofit(@Named("baseClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApi(@Named("weatherRetrofit") retrofit: Retrofit): WeatherApi = 
        retrofit.create(WeatherApi::class.java)

    @Provides
    @Singleton
    fun provideVehicleSessionApi(@Named("authenticatedRetrofit") retrofit: Retrofit): VehicleSessionApi = 
        retrofit.create(VehicleSessionApi::class.java)

    @Provides
    @Singleton
    fun provideBusinessApi(@Named("authenticatedRetrofit") retrofit: Retrofit): BusinessApi =
        retrofit.create(BusinessApi::class.java)

    @Provides
    @Singleton
    fun provideBusinessConfigurationApi(@Named("authenticatedRetrofit") retrofit: Retrofit): BusinessConfigurationApi =
        retrofit.create(BusinessConfigurationApi::class.java)

    @Provides
    @Singleton
    fun provideUserBusinessApi(@Named("authenticatedRetrofit") retrofit: Retrofit): UserBusinessApi =
        retrofit.create(UserBusinessApi::class.java)

    @Provides
    @Singleton
    fun provideCountryApi(@Named("authenticatedRetrofit") retrofit: Retrofit): CountryApi =
        retrofit.create(CountryApi::class.java)

    @Provides
    @Singleton
    fun provideStateApi(@Named("authenticatedRetrofit") retrofit: Retrofit): CountryStateApi =
        retrofit.create(CountryStateApi::class.java)

    @Provides
    @Singleton
    fun provideEnergySourceApi(@Named("authenticatedRetrofit") retrofit: Retrofit): EnergySourceApi =
        retrofit.create(EnergySourceApi::class.java)

    @Provides
    @Singleton
    fun provideSiteApi(@Named("authenticatedRetrofit") retrofit: Retrofit): SiteApi =
        retrofit.create(SiteApi::class.java)

    @Provides
    @Singleton
    fun provideVehicleComponentApi(@Named("authenticatedRetrofit") retrofit: Retrofit): VehicleComponentApi =
        retrofit.create(VehicleComponentApi::class.java)

    @Provides
    @Singleton
    fun provideFileUploaderApi(@Named("authenticatedRetrofit") retrofit: Retrofit): FileUploaderApi =
        retrofit.create(FileUploaderApi::class.java)

    @Provides
    @Singleton
    fun provideIncidentMultimediaRepository(
        api: IncidentMultimediaApi,
        businessContextManager: BusinessContextManager
    ): IncidentMultimediaRepository = IncidentMultimediaRepositoryImpl(api, businessContextManager)

    @Provides
    @Singleton
    fun provideIncidentMultimediaApi(@Named("authenticatedRetrofit") retrofit: Retrofit): IncidentMultimediaApi =
        retrofit.create(IncidentMultimediaApi::class.java)

    @Provides
    @Singleton
    fun provideGOGroupApi(@Named("authenticatedRetrofit") retrofit: Retrofit): GOGroupApi =
        retrofit.create(GOGroupApi::class.java)

    @Provides
    @Singleton
    fun provideGOGroupRoleApi(@Named("authenticatedRetrofit") retrofit: Retrofit): GOGroupRoleApi =
        retrofit.create(GOGroupRoleApi::class.java)

    @Provides
    @Singleton
    fun provideGOUserRoleApi(@Named("authenticatedRetrofit") retrofit: Retrofit): GOUserRoleApi =
        retrofit.create(GOUserRoleApi::class.java)

    @Provides
    @Named("baseUrl")
    fun provideBaseUrl(): String = Constants.BASE_URL

    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(
        @ApplicationContext context: Context
    ): NetworkConnectivityManager {
        return NetworkConnectivityManager(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideVehicleFailIncidentApi(
        @Named("authenticatedRetrofit") authenticatedRetrofit: Retrofit
    ): VehicleFailIncidentApi {
        return authenticatedRetrofit.create(VehicleFailIncidentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChecklistItemAnswerMultimediaApi(@Named("authenticatedRetrofit") retrofit: Retrofit): ChecklistItemAnswerMultimediaApi =
        retrofit.create(ChecklistItemAnswerMultimediaApi::class.java)

    @Provides
    @Singleton
    fun provideChecklistItemAnswerMultimediaRepository(
        api: ChecklistItemAnswerMultimediaApi,
        headerManager: HeaderManager,
        businessContextManager: BusinessContextManager
    ): ChecklistItemAnswerMultimediaRepository = ChecklistItemAnswerMultimediaRepositoryImpl(api, headerManager, businessContextManager)
}