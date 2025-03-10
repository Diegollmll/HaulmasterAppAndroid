package app.forku.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {
    // Remove this method:
    /*
    @Provides
    @Singleton
    fun provideSessionRepository(
        api: Sub7Api,
        authDataStore: AuthDataStore
    ): SessionRepository {
        return SessionRepositoryImpl(api, authDataStore)
    }
    */
} 