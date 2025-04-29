package app.forku.di

import app.forku.data.api.GOServicesApi
import app.forku.data.api.GOSecurityProviderApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.datastore.GOServicesPreferences
import app.forku.data.repository.GOServicesRepository
import app.forku.data.repository.GOSecurityProviderRepository
import app.forku.data.service.GOServicesManager
import app.forku.domain.repository.IGOServicesRepository
import app.forku.domain.repository.IGOSecurityProviderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object GOServicesModule {

    @Provides
    @Singleton
    fun provideGOServicesApi(retrofit: Retrofit): GOServicesApi {
        return retrofit.create(GOServicesApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGOSecurityProviderApi(retrofit: Retrofit): GOSecurityProviderApi {
        return retrofit.create(GOSecurityProviderApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGOServicesRepository(
        api: GOServicesApi,
        authDataStore: AuthDataStore
    ): IGOServicesRepository {
        return GOServicesRepository(api, authDataStore)
    }

    @Provides
    @Singleton
    fun provideGOSecurityProviderRepository(
        api: GOSecurityProviderApi,
        authDataStore: AuthDataStore,
        okHttpClient: OkHttpClient,
        retrofit: Retrofit
    ): IGOSecurityProviderRepository {
        return GOSecurityProviderRepository(api, authDataStore, okHttpClient, retrofit)
    }

    @Provides
    @Singleton
    fun provideGOServicesManager(
        repository: IGOServicesRepository,
        authDataStore: AuthDataStore
    ): GOServicesManager {
        return GOServicesManager(repository, authDataStore)
    }
} 