package app.forku.di

import app.forku.data.api.Sub7Api
import app.forku.data.datastore.AuthDataStore
import app.forku.data.repository.session.SessionRepositoryImpl
import app.forku.domain.repository.session.SessionRepository
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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