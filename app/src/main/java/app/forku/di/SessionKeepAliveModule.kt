package app.forku.di

import android.content.Context
import app.forku.core.auth.SessionKeepAliveManager
import app.forku.core.auth.HeaderManager
import app.forku.data.api.GOSecurityProviderApi
import app.forku.data.datastore.AuthDataStore
import app.forku.domain.repository.IGOSecurityProviderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import javax.inject.Provider

@Module
@InstallIn(SingletonComponent::class)
object SessionKeepAliveModule {

    @Provides
    @Singleton
    fun provideSessionKeepAliveManager(
        goSecurityApi: GOSecurityProviderApi,
        goSecurityProviderRepository: Provider<IGOSecurityProviderRepository>,
        headerManager: HeaderManager,
        authDataStore: AuthDataStore,
        @ApplicationContext context: Context
    ): SessionKeepAliveManager {
        return SessionKeepAliveManager(goSecurityApi, goSecurityProviderRepository, headerManager, authDataStore, context)
    }
} 